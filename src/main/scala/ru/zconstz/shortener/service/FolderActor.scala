package ru.zconstz.shortener.service

import akka.actor.Actor
import ru.zconstz.shortener.http.HttpEntities._
import ru.zconstz.shortener.db.DbHolder._
import scala.slick.driver.PostgresDriver.simple._
import ru.zconstz.shortener.db.DataBaseSchema.{Links, Folders}

class FolderActor extends Actor {
  def receive = {
    case FolderGetRequest(token) => {
      sender ! dataBase.withSession {
        implicit session: Session =>
        (for {
          folder <- Folders
          user <- folder.user if user.token === token
        } yield (folder.id, folder.title)).list().map {
          case (id, title) => Folder(id, title)
        }
      }
    }
    case (id, FolderByIdGetRequest(token, offset, limit)) => {
      sender ! dataBase.withSession {
        implicit session: Session =>
        (for {
          link <- Links
          folder <- link.folder
        } yield (link.url, link.code)).drop(offset.getOrElse(0)).take(limit.getOrElse(defaultLimit)).list().map {
          case (url, code) => Link(url, code)
        }
      }
    }
  }
}
