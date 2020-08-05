package com.oceanum.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.oceanum.common.{ActorSystems, Environment, SystemInit}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/8/4
 */
object HttpClient {
  implicit private val system: ActorSystem = ActorSystems.SYSTEM
  implicit private val material: ActorMaterializer = ActorMaterializer()
  implicit private val executionContext: ExecutionContextExecutor = Environment.NONE_BLOCKING_EXECUTION_CONTEXT
  private val serialization = SystemInit.serialization

  private val port: Int = Environment.REST_SERVER_PORT

  def execute[T<:AnyRef, P<:AnyRef](path: String, method: HttpMethod = HttpMethods.GET, param: Map[String, String] = Map.empty, entity: Option[T] = None)(implicit mf: Manifest[P]): Future[P] = {
    Http().singleRequest(HttpRequest(
      uri = Uri(path)
        .withQuery(Uri.Query(param)),
      method = method,
      entity = entity match {
        case Some(t) => HttpEntity(ContentTypes.`application/json`, serialization.serialize(t))
        case None => HttpEntity.empty(ContentTypes.`application/json`)
      }
    ))
      .flatMap { res =>
        if (res.status.isFailure()) {
          Future.failed(new Exception(res.toString()))

        } else if (mf.equals(Manifest.Nothing)) {
            Future.successful(null.asInstanceOf[P])

        } else {
          Unmarshal(res.entity).to[String]
            .map(serialization.deSerializeRaw(_)(mf))
        }
      }
  }

  def get[T<:AnyRef, P<:AnyRef](url: String, param: Map[String, String] = Map.empty, entity: Option[T] = None)(implicit mf: Manifest[P]): Future[P] = {
    execute(url, HttpMethods.GET, param, entity)
  }

  def post[T<:AnyRef, P<:AnyRef](url: String, param: Map[String, String] = Map.empty, entity: Option[T] = None)(implicit mf: Manifest[P]): Future[P] = {
    execute(url, HttpMethods.POST, param, entity)
  }

  def put[T<:AnyRef, P<:AnyRef](url: String, param: Map[String, String] = Map.empty, entity: Option[T] = None)(implicit mf: Manifest[P]): Future[P] = {
    execute(url, HttpMethods.PUT, param, entity)
  }

  def delete[T<:AnyRef, P<:AnyRef](url: String, param: Map[String, String] = Map.empty, entity: Option[T] = None)(implicit mf: Manifest[P]): Future[P] = {
    execute(url, HttpMethods.DELETE, param, entity)
  }
}
