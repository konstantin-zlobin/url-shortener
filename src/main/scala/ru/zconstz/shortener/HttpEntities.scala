package ru.zconstz.shortener

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

object HttpEntities {
  val const = 100

  case class Link(url: String, code: String) extends SprayJsonSupport

  case class TokenGetRequest(userId: String, secret: String)
  case class TokenGetResponse(token: String) extends SprayJsonSupport

  case class LinkPostRequest(token: String, url: String, code: Option[String], folderId: Option[String])
  type LinkPostResponse = Link

  case class LinkByCodePostRequest(referrer: String, remoteIp: String, otherStats: Option[String])
  case class LinkByCodePostResponse(linkPathThrough: String) extends SprayJsonSupport

  case class LinkByCodeGetRequest(token:String)
  case class LinkByCodeGetResponse(link: Link, folderId: Option[String], clicks: Int) extends SprayJsonSupport

  case class FolderByIdGetRequest(token: String, offset: Option[Int] = Some(0), limit: Option[Int] = Some(const))
  type FolderByIdGetResponse = List[Link]

  case class LinkGetRequest(token:String, offset: Option[Int] = Some(0), limit: Option[Int] = Some(const))
  type LinkGetResponse = List[Link]

  case class Folder(id: String, title: String) extends SprayJsonSupport

  case class FolderGetRequest(token: String)
  type FolderGetResponse = List[Folder]

  case class Click(date: String, referrer: String, remoteIp: String) extends SprayJsonSupport

  case class LinkByCodeClicksGetRequest(token: String, offset: Int, limit: Int)
  type LinkCodeClickGetResponse = List[Click]

  object JsonProtocol extends DefaultJsonProtocol {
    implicit val _tokenGetResponse = jsonFormat1(TokenGetResponse)
    implicit val _linkResponse = jsonFormat2(Link)
    implicit val _linkByCodePostResponse = jsonFormat1(LinkByCodePostResponse)
    implicit val _linkByCodeGetResponse = jsonFormat3(LinkByCodeGetResponse)
    implicit val _folderResponse = jsonFormat2(Folder)
    implicit val _clickResponse = jsonFormat3(Click)
  }
}
