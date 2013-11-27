package ru.zconstz.shortener

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import scala.slick.session.{Session, Database}
import com.typesafe.config.{ConfigFactory, Config}

object Boot extends App {

  val conf:Config  = ConfigFactory.load()
  val dataBase = Database.forURL(conf.getString("database.url"),
    driver = conf.getString("database.driver"),
    user = conf.getString("database.user"),
    password = conf.getString("database.password"))

  dataBase.withSession { implicit session:Session =>
    DataBaseEntities.reCreateSchema()
    DataBaseEntities.insertTestData()
  }

  implicit val system = ActorSystem("url-shortener")

  val service = system.actorOf(Props[UrlShortenerActor], "url-shortener-service")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}