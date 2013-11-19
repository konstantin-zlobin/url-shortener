package ru.zconstz.shortener

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport

class UrlShortenerActor extends Actor with UrlShortenerService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

case class Entity2(value:String, value2: String) extends SprayJsonSupport

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat2(Entity2)
}
import MyJsonProtocol._

trait UrlShortenerService extends HttpService {

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
    path("json"){
      get {
        respondWithMediaType(`application/json`) {
          complete(Entity2("10", "20"))
        }
      }
    }
}