package ru.zconstz.shortener

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import scala.slick.session.Session
import ru.zconstz.shortener.db.{DbHolder, DataBaseSchema}
import ru.zconstz.shortener.http.UrlShortenerActor

object Boot extends App {

  DbHolder.dataBase.withSession {
    implicit session: Session =>
      DataBaseSchema.reCreate()
      DataBaseSchema.insertTestData()
  }

  implicit val system = ActorSystem("url-shortener")

  val service = system.actorOf(Props[UrlShortenerActor], "url-shortener-service")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}