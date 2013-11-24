package ru.zconstz.shortener

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

class UrlShortenerActor extends Actor with UrlShortenerService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}


trait UrlShortenerService extends HttpService {

  import HttpEntities._
  import HttpEntities.JsonProtocol._

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~
    path("token") {
      get {
        parameters("user_id", "secret").as(TokenGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete(TokenGetResponse(s"stupid token: ${request.userId}-${request.secret}"))
          }
        }
      }
    } ~
    path("link") {
      get {
        parameters("token", "offset"?, "limit"?).as(LinkGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete(List(Link("123", "http://www.google.com"), Link("234", "http://www.yahoo.com")))
          }
        }
      } ~
      post {
        entity(as[LinkPostRequest]) { request =>
          respondWithMediaType(`application/json`) {
            complete(List(Link("123", "http://www.google.com")))
          }
        }
      }
    } ~
    path("link" / Segment) { code =>
      get {
        parameter("token").as(LinkByCodeGetRequest) { request =>
          respondWithMediaType(`application/json`) {
            complete(LinkByCodeGetResponse(Link(s"$code", "http://www.google.com"), Some("folderId"), 10))
          }
        }
      } ~
      post {
        entity(as[LinkByCodePostRequest]) { request =>
          respondWithMediaType(`application/json`) {
            complete(LinkByCodePostResponse("linkPathThrough"))
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