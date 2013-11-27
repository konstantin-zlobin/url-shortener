package ru.zconstz.shortener.http

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import akka.pattern.ask
import akka.util.Timeout
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import ru.zconstz.shortener.service.ServiceRefs
import reflect.ClassTag

class UrlShortenerActor extends Actor with UrlShortenerService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}


trait UrlShortenerService extends HttpService with ServiceRefs {

  import HttpEntities._
  import HttpEntities.JsonProtocol._

  implicit val timeout = Timeout(5 seconds)

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <h1>Say hello to <i>url-shortener</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~
    path("token") {
      get {
        parameters("user_id".as[Long], "secret").as(TokenGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete {
              (tokenActor ? request).mapTo[TokenGetResponse]
            }
          }
        }
      }
    } ~
    path("link") {
      get {
        parameters("token", "offset"?, "limit"?).as(LinkGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete {
              (linkActor ? request).mapTo[List[Link]]
            }
          }
        }
      } ~
      post {
        entity(as[LinkPostRequest]) { request =>
          respondWithMediaType(`application/json`) {
            complete {
              (linkActor ? request).mapTo[Either[String, Link]]
            }
          }
        }
      }
    } ~
    path("link" / Segment) { code =>
      get {
        parameter("token").as(LinkByCodeGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete {
              (linkActor ? (code, request)).mapTo[Either[String, LinkByCodeGetResponse]]
            }
          }
        }
      } ~
      post {
        entity(as[LinkByCodePostRequest]) { request =>
          respondWithMediaType(`application/json`) {
            complete {
              (clicksActor ? (code, request)).mapTo[Either[String, LinkByCodePostResponse]]
            }
          }
        }
      }
    } ~
    path("link" / Segment / "clicks") { code =>
      get {
        parameters("token", "offset", "limit").as(LinkByCodeClicksGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete(List(Click("11", "22", "33"), Click("11", "22", "33")))
          }
        }
      }
    } ~
    path("folder") {
      get {
        parameters("token").as(FolderGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete(List(Folder("1223", "shortens")))
          }
        }
      }
    } ~
    path("folder" / Segment) { id =>
      get {
        parameters("token", "offset"?, "limit"?).as(FolderByIdGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete(List(Link("1223", "http://shortens")))
          }
        }
      }
    }
}
