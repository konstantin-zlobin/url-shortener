package ru.zconstz.shortener.service

import akka.actor.Actor
import scala.slick.driver.PostgresDriver.simple._
import ru.zconstz.shortener.db.{DbHolder, DataBaseEntities}
import DataBaseEntities._
import DbHolder._
import ru.zconstz.shortener.http.HttpEntities.{TokenGetResponse, TokenGetRequest}

class TokenActor extends Actor {
  def receive = {
    case TokenGetRequest(userId, secret) => {
      sender ! dataBase.withSession { implicit session:Session =>
        Query(Users).where(_.id === userId).firstOption.map(user => TokenGetResponse(user._2))
      }
    }
  }
}
