package ru.zconstz.shortener.service

import akka.actor.Actor
import ru.zconstz.shortener.http.HttpEntities._
import ru.zconstz.shortener.db.DbHolder._
import scala.slick.driver.PostgresDriver.simple._
import ru.zconstz.shortener.db.DataBaseSchema.{Folders, Clicks, Users, Links}
import scala.annotation.tailrec
import ru.zconstz.shortener.http.HttpEntities.LinkGetRequest
import ru.zconstz.shortener.http.HttpEntities.LinkPostRequest
import ru.zconstz.shortener.http.HttpEntities.Link

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
    case LinkPostRequest(token, url, proposedCode, folderOptionId) => {
      sender ! dataBase.withTransaction {
        implicit session: Session =>
          def codeDoesntExist(code: String): Boolean = Query(Links).where(_.code === code).firstOption.isEmpty
          Query(Users).where(_.token === token).firstOption() match {
            case Some(user) => {
              val newLinkCode = proposedCode match {
                case Some(code) => if (codeDoesntExist(code)) generateUniqueCodeForUrl(url)(codeDoesntExist) else code
                case None => generateUniqueCodeForUrl(url)(codeDoesntExist)
              }
              folderOptionId match {
                case Some(folderId) if Query(Folders).where(_.id === folderId).firstOption.isEmpty =>
                  Left("Error: Unknown folder")
                case _ => {
                  Links.autoInc.insert(None, url, newLinkCode, user._1, folderOptionId)
                  Right(Link(url, newLinkCode))
                }
              }
            }
            case None => Left("Error: Unknown user")
          }
      }
    }
    case (code: String, LinkByCodeGetRequest(token)) => {
      sender ! dataBase.withSession {
        implicit session: Session =>
          (for {
            click <- Clicks
            link <- click.link if link.code === code
            user <- link.user if user.token === token
          } yield (link.url, link.code, link.folderId, click.countDistinct)).firstOption() match {
            case Some((url, linkCode, folderId, countClicks)) =>
              Right(LinkByCodeGetResponse(Link(url, linkCode), folderId, countClicks))
            case None => Left(s"Error: Link with code == $code was not found!")
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
