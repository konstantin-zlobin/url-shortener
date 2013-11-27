package ru.zconstz.shortener.service

import akka.actor.Actor
import ru.zconstz.shortener.http.HttpEntities.{LinkPostRequest, Link, LinkGetRequest}
import ru.zconstz.shortener.db.DbHolder._
import scala.slick.driver.PostgresDriver.simple._
import ru.zconstz.shortener.db.DataBaseEntities.{Users, Links}
import scala.annotation.tailrec

class LinkActor extends Actor {

  def receive = {
    case LinkGetRequest(token, offset, limit) => {
      sender ! dataBase.withSession {
        implicit session: Session =>
          (for {
            link <- Links
            user <- link.user if user.token === token
          } yield (link.code, link.url)).drop(offset.getOrElse(0)).take(limit.getOrElse(defaultLimit)).list()
            .map(link => Link(link._1, link._2))
      }
    }
    case LinkPostRequest(token, url, proposedCode, folder_id) => {
      sender ! dataBase.withTransaction {
        implicit session: Session =>
          def codeExist(code: String): Boolean = !Query(Links).where(_.code === code).firstOption.isDefined
          Query(Users).where(_.token === token).firstOption() match {
            case Some(user) => {
              val newLinkCode = proposedCode match {
                case Some(code) => if (codeExist(code)) generateUniqueCodeForUrl(url)(codeExist) else code
                case None => generateUniqueCodeForUrl(url)(codeExist)
              }
              Links.autoInc.insert(None, url, newLinkCode, user._1, folder_id)
              Right(Link(url, newLinkCode))
            }
            case None => Left("Eror: Unknown user")
          }
      }
    }
  }

  @tailrec
  private def generateUniqueCodeForUrl(url: String)(uniquenessChecker: String => Boolean): String = {
    val generatedCode = generateCodeForUrl(url)
    if (uniquenessChecker(generatedCode)) generatedCode else generateUniqueCodeForUrl(url)(uniquenessChecker)
  }

  private val alphabet = (Range('a', 'z') ++ Range('A', 'Z') ++ Range('0', '9')).toStream

  private def generateCodeForUrl(url: String): String = scala.util.Random.shuffle(alphabet).take(6).mkString
}
