package ru.zconstz.shortener.service

import akka.actor.Actor
import ru.zconstz.shortener.http.HttpEntities._
import ru.zconstz.shortener.db.DbHolder._
import scala.slick.driver.PostgresDriver.simple._
import ru.zconstz.shortener.db.DataBaseSchema.{Clicks, Links}
import java.sql.Date

class ClickActor extends Actor {
  def receive = {
    case (code: String, LinkByCodePostRequest(referer, remoteIp, otherStats)) => {
      sender ! dataBase.withTransaction {
        implicit session: Session =>
          Query(Links).where(_.code === code).firstOption match {
            case Some((id, url, linkCode, userId, folderId)) => {
              Clicks.autoInc.insert(new Date(new java.util.Date().getTime), referer, remoteIp, otherStats, id)
              Right(LinkByCodePostResponse(url))
            }
            case None => Left(s"Error: Link with code = $code is not found!")
          }
      }
    }
    case (code:String, LinkByCodeClicksGetRequest(token, offset, limit)) => {
      sender ! dataBase.withSession {
        implicit session: Session =>
          (for {
            click <- Clicks
            link <- click.link if link.code === code
            user <- link.user if user.token === token
          } yield (click.date, click.referer, click.remoteIp)).drop(offset).take(limit).list().map {
            case (date, referer, remoteIp) => Click(date, referer, remoteIp)
          }
      }
    }
  }
}
