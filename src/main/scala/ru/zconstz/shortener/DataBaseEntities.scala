package ru.zconstz.shortener

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ColumnOption.{Nullable, NotNull}
import java.sql.Date

object DataBaseEntities {

  object Users extends Table[(Long, String)]("USERS") {
    def id = column[Long]("ID", O PrimaryKey, O AutoInc)
    def token = column[String]("TOKEN", NotNull)
    def * = id ~ token
  }

  object Folders extends Table[(Long, String, Long)]("FOLDERS") {
    def id = column[Long]("ID", O PrimaryKey, O AutoInc)
    def title = column[String]("TITLE", NotNull)
    def userId = column[Long]("USER_ID", NotNull)
    def * = id ~ title ~ userId
    def user = foreignKey("USER_FK", userId, Users)(_.id)
  }

  object Links extends Table[(Long, String, String, Long, Option[Long])]("LINKS") {
    def id = column[Long]("ID", O PrimaryKey, O AutoInc)
    def url = column[String]("URL", NotNull)
    def code = column[String]("CODE", NotNull)
    def userId = column[Long]("USER_ID", NotNull)
    def folderId = column[Option[Long]]("FOLDER_ID", Nullable)
    def * = id ~ url ~ code ~ userId ~ folderId
    def user = foreignKey("USER_FK", userId, Users)(_.id)
    def folder = foreignKey("FOLDER_FK", folderId, Folders)(_.id)
  }

  object Clicks extends Table[(Long, Date, String, String, Long)]("CLICKS") {
    def id = column[Long]("ID", O PrimaryKey, O AutoInc)
    def date = column[Date]("DATE", NotNull)
    def referrer = column[String]("REFERRER", NotNull)
    def remoteIp = column[String]("REMOTE_IP", NotNull)
    def linkId = column[Long]("LINK_ID", NotNull)
    def * = id ~ date ~ referrer ~ remoteIp ~ linkId
    def link = foreignKey("LINK_FK", linkId, Links)(_.id)
  }
}
