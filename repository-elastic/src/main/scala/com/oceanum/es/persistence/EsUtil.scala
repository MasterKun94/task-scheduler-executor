package com.oceanum.es.persistence

import com.oceanum.common.Environment
import com.oceanum.es.expr._
import com.oceanum.expr._
import com.oceanum.serialize.Serialization
import org.apache.http.HttpHost
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.admin.indices.create.{CreateIndexRequest, CreateIndexResponse}
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}

class EsUtil {

}

object EsUtil {
  private val hostsKey = "es.hosts"
  private val typ = "type"
  private lazy val hosts = Environment.getProperty(hostsKey, "localhost:9200")
    .split(",")
    .map(_.trim.split(":"))
    .map(arr => if (arr.length == 1) new HttpHost(arr(0)) else new HttpHost(arr(0), arr(1).toInt))

  private lazy val client = new RestHighLevelClient(
    RestClient.builder(hosts:_*)
  )
  private lazy val serialization = Serialization.default
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  def save[T<:AnyRef](idx: String, id: String, t: T): Future[Unit] = {
    val req = new IndexRequest(idx, typ, id)
      .source(serialization.serialize(t), XContentType.JSON)
    val promise = Promise[Unit]()
    client.indexAsync(req, RequestOptions.DEFAULT, Listener[IndexResponse, Unit](promise)(_ => Unit))
    promise.future
  }

  def save[T<:AnyRef](idx: String, t: T): Future[String] = {
    val req = new IndexRequest(idx, typ)
      .source(serialization.serialize(t), XContentType.JSON)
    val promise = Promise[String]()
    client.indexAsync(req, RequestOptions.DEFAULT, Listener[IndexResponse, String](promise)(_.getId))
    promise.future
  }

  def saveAll[T<:AnyRef](idx: String, objs: Seq[(String, T)]): Future[Unit] = {
    if (objs.isEmpty) {
      return Future.successful(Unit)
    }
    val req = new BulkRequest()
    for ((id, t) <- objs) {
      req.add(new IndexRequest(idx, typ, id).source(serialization.serialize(t), XContentType.JSON))
    }
    val promise = Promise[Unit]()
    client.bulkAsync(req, RequestOptions.DEFAULT, Listener[BulkResponse, Unit](promise) { res =>
      res.getItems.find(_.isFailed) match {
        case Some(res) => res.getFailure.getCause
        case None => Unit
      }
    })
    promise.future
  }

  def findById[T<:AnyRef](idx: String, id: String)(implicit mf: Manifest[T]): Future[Option[T]] = {
    val req = new GetRequest(idx, typ, id)
    val promise = Promise[Option[T]]()
    client.getAsync(req, RequestOptions.DEFAULT, Listener[GetResponse, Option[T]](promise) { res =>
      if (res.isExists) {
        Some(serialization.deSerializeRaw(res.getSourceAsString)(mf))
      } else {
        None
      }
    })
    promise.future
  }

  def findByIdIn[T<:AnyRef](idx: String, ids: Seq[String])(implicit mf: Manifest[T]): Future[Seq[T]] = {
    val req = new SearchRequest()
      .indices(idx)
      .types(typ)
      .source(new SearchSourceBuilder()
        .query(QueryBuilders.idsQuery().addIds(ids:_*))
      )
    val promise = Promise[Seq[T]]()
    client.searchAsync(req, RequestOptions.DEFAULT, Listener[SearchResponse, Seq[T]](promise) { res =>
      res.getHits
        .getHits
        .map(res => serialization.deSerializeRaw(res.getSourceAsString)(mf))
    })
    promise.future
  }

  def find[T<:AnyRef](idx: String, expr: String, env: JavaMap[String, AnyRef])(implicit mf: Manifest[T]): Future[Seq[T]] = {
    val req = new SearchRequest(idx)
      .indices(idx)
      .types(typ)
      .source(parseExpr(expr, env))
    val promise = Promise[Seq[T]]()
    client.searchAsync(req, RequestOptions.DEFAULT, Listener[SearchResponse, Seq[T]](promise) { res =>
      res.getHits
        .getHits
        .map(hit => serialization.deSerializeRaw(hit.getSourceAsString)(mf))
    })
    promise.future
  }

  def parseExpr(expr: String, env: JavaMap[String, AnyRef] = new JavaHashMap(0)): SearchSourceBuilder = {
    val obj = Evaluator.rawExecute(expr, env)
    val builder: SearchSourceBuilder = obj match {
      case SearchSourceExpression(searchSourceBuilder) => searchSourceBuilder
      case QueryExpression(queryBuilder) => new SearchSourceBuilder().query(queryBuilder)
      case RangeExpression(rangeQueryBuilder) => new SearchSourceBuilder().query(rangeQueryBuilder)
      case SizeExpression(size) => new SearchSourceBuilder().size(size)
      case PageExpression(page) =>
        val builder = new SearchSourceBuilder()
        builder.from(page * builder.size())
    }
    builder
  }

  def createIndex(index: String): Future[Unit] = {
    val promise = Promise[Unit]()
    client.indices().createAsync(new CreateIndexRequest(index), RequestOptions.DEFAULT, Listener[CreateIndexResponse, Unit](promise) { _ =>
      promise.success(Unit)
    })
    promise.future
  }
}

class Listener[T, OUT](promise: Promise[OUT], func: T => OUT) extends ActionListener[T] {
  override def onResponse(response: T): Unit = {
    func(response) match {
      case e: Throwable => promise.failure(e)
      case out => promise.success(out)
    }
  }
  override def onFailure(e: Exception): Unit = promise.failure(e)
}

object Listener {
  def apply[T, OUT](promise: Promise[OUT])(func: T => OUT): Listener[T, OUT] = new Listener(promise, func)
}