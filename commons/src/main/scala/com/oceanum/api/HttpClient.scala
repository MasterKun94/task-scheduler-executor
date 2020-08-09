package com.oceanum.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.oceanum.common.{ActorSystems, Environment, SystemInit}
import com.oceanum.exceptions.RemoteException
import com.oceanum.serialize.WrappedException

import scala.concurrent.{ExecutionContextExecutor, Future}

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
          if (res.entity.isKnownEmpty() || res.entity.contentType != ContentTypes.`application/json`) {
            Future.failed(new RemoteException(res.toString()))
          } else {
            Unmarshal(res.entity)
              .to[String]
              .map(serialization.deSerializeRaw[Throwable](_))
              .map(e => throw new RemoteException(res.toString(), e))
          }


        } else if (mf.runtimeClass.equals(classOf[String])) {
          Unmarshal(res.entity).to[String].map(_.asInstanceOf[P])

        } else {
          mf match {
            case Manifest.Nothing => Future.successful(null.asInstanceOf[P])
            case _ => Unmarshal(res.entity).to[String].map(serialization.deSerializeRaw[P](_)(mf))
          }
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
