package ru.zconstz.shortener

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {

  implicit val system = ActorSystem("url-shortener")

  val service = system.actorOf(Props[UrlShortenerActor], "url-shortener-service")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}
