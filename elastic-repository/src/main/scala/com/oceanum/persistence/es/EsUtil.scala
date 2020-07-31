package com.oceanum.persistence.es

import com.oceanum.annotation.util.AnnotationUtil
import com.oceanum.serialize.Serialization
import org.apache.http.HttpHost
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}

class EsUtil {

}

object EsUtil {
  private lazy val client = new RestHighLevelClient(
    RestClient.builder(
      new HttpHost("192.168.10.132", 9200)
    )
  )
  private lazy val serialization = Serialization.default
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  def save[T<:AnyRef](idx: String, id: String, t: T): Future[Unit] = {
    val req = new IndexRequest(idx, "type", id)
      .source(serialization.serialize[T](t), XContentType.JSON)
    val promise = Promise[Unit]()
    client.indexAsync(req, RequestOptions.DEFAULT, Listener[IndexResponse, Unit](promise)(_ => Unit))
    promise.future
  }

  def findById[T<:AnyRef](idx: String, id: String)(implicit mf: Manifest[T]): Future[Option[T]] = {
    val req = new GetRequest(idx, "type", id)
    val promise = Promise[Option[T]]()
    client.getAsync(req, RequestOptions.DEFAULT, Listener[GetResponse, Option[T]](promise) { res =>
      if (res.isExists) {
        println(res.getSourceAsString)
        Some(serialization.deSerializeRaw(res.getSourceAsString)(mf))
      } else {
        None
      }
    })
    promise.future
  }

  def find[T<:AnyRef](idx: String, expr: String)(implicit mf: Manifest[T]): Future[Array[T]] = {
    val req = new SearchRequest(idx).source(parseExpr(expr))
    val promise = Promise[Array[T]]()
    client.searchAsync(req, RequestOptions.DEFAULT, Listener[SearchResponse, Array[T]](promise) { res =>
      res.getHits.getHits.map(hit => serialization.deSerializeRaw(hit.getSourceAsString)(mf))
    })
    promise.future
  }

  def parseExpr(expr: String): SearchSourceBuilder = ???


  def main(args: Array[String]): Unit = {
//    println(AnnotationUtil.jobWithAnnotation)
  }
}

class Listener[T, OUT](promise: Promise[OUT], func: T => OUT) extends ActionListener[T] {
  override def onResponse(response: T): Unit = promise.success(func(response))
  override def onFailure(e: Exception): Unit = promise.failure(e)
}

object Listener {
  def apply[T, OUT](promise: Promise[OUT])(func: T => OUT): Listener[T, OUT] = new Listener(promise, func)
}