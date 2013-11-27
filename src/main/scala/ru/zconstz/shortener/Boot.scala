package ru.zconstz.shortener

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import scala.slick.session.Session
import ru.zconstz.shortener.db.{DbHolder, DataBaseEntities}
import ru.zconstz.shortener.http.UrlShortenerActor

object Boot extends App {

  DbHolder.dataBase.withSession {
    implicit session: Session =>
      DataBaseEntities.reCreateSchema()
      DataBaseEntities.insertTestData()
  }

  implicit val system = ActorSystem("url-shortener")

  val service = system.actorOf(Props[UrlShortenerActor], "url-shortener-service")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}