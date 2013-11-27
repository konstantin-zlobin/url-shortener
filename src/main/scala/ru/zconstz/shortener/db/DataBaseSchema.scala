package ru.zconstz.shortener.db

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ColumnOption.{Nullable, NotNull}
import java.sql.Date
import scala.slick.session.Session
import scala.slick.jdbc.meta.MTable

object DataBaseSchema {

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

    def autoInc = id.? ~ title ~ userId returning id

    def user = foreignKey("USER_FK", userId, Users)(_.id)
  }

  object Links extends Table[(Long, String, String, Long, Option[Long])]("LINKS") {
    def id = column[Long]("ID", O PrimaryKey, O AutoInc)

    def url = column[String]("URL", NotNull)

    def code = column[String]("CODE", NotNull)

    def userId = column[Long]("USER_ID", NotNull)

    def folderId = column[Option[Long]]("FOLDER_ID", Nullable)

    def * = id ~ url ~ code ~ userId ~ folderId

    def autoInc = id.? ~ url ~ code ~ userId ~ folderId returning id

    def user = foreignKey("USER_FK", userId, Users)(_.id)

    def folder = foreignKey("FOLDER_FK", folderId, Folders)(_.id)
  }

  object Clicks extends Table[(Long, Date, String, String, Option[String], Long)]("CLICKS") {
    def id = column[Long]("ID", O PrimaryKey, O AutoInc)

    def date = column[Date]("DATE", NotNull)

    def referer = column[String]("REFERER", NotNull)

    def remoteIp = column[String]("REMOTE_IP", NotNull)

    def otherStats = column[Option[String]]("OTHER_STATS", Nullable)

    def linkId = column[Long]("LINK_ID", NotNull)

    def * = id ~ date ~ referer ~ remoteIp ~ otherStats ~ linkId

    def autoInc = id.? ~ date ~ referer ~ remoteIp ~ otherStats ~ linkId returning id

    def link = foreignKey("LINK_FK", linkId, Links)(_.id)
  }

  def reCreate()(implicit session: Session) {
    val tableList = List(Users, Folders, Links, Clicks)
    for {
      table <- tableList.reverse
      if !MTable.getTables(table.tableName).list().isEmpty
    } table.ddl.drop
    tableList.foreach { table =>
      table.ddl.create
    }
  }

  def insertTestData()(implicit session: Session) {
    Users.insert(1, "a213ffab423def31")
    Users.insert(2, "7172bcde78909fed")

    Folders.insert(10, "FAVORITES", 1)
    Folders.insert(11, "Trash", 1)
    Folders.insert(12, "Fun", 2)
    Folders.insert(13, "serious", 2)

    Links.insert(100, "http://en.wikipedia.org/wiki/URL_shortening#Expiry_and_time-limited_services",
      "x5D7J", 1, Some(10))
    Links.insert(101, "http://www.thetimes.co.uk/tto/technology/article1859818.ece",
      "p78D3", 1, Some(11))
    Links.insert(102, "http://www.dmoz.org/Computers/Internet/Web_Design_and_Development/Hosted_Components_and_Services/Redirects/",
      "bI8l0", 2, Some(12))
    Links.insert(103, "http://searchengineland.com/analysis-which-url-shortening-service-should-you-use-17204",
      "paRL8", 2, Some(13))
    Links.insert(104, "http://en.blog.wordpress.com/2009/08/14/shorten/",
      "GUsh9", 1, None)
    Links.insert(105, "http://en.wikipedia.org/wiki/Country_code_top-level_domain",
      "kul78", 2, None)

    Clicks.insert(1000, Date.valueOf("2013-11-15"), "http://google.com", "10.67.32.101", Some("adroid_id: 786238afgbcd767"), 100)
    Clicks.insert(1001, Date.valueOf("2013-11-16"), "http://google.com", "10.67.32.102", None, 101)
    Clicks.insert(1002, Date.valueOf("2013-11-17"), "http://google.com", "10.67.32.103", None, 102)
    Clicks.insert(1003, Date.valueOf("2013-11-18"), "http://google.com", "10.67.32.104", None, 103)
    Clicks.insert(1004, Date.valueOf("2013-11-19"), "http://google.com", "10.67.32.105", None, 104)
    Clicks.insert(1005, Date.valueOf("2013-11-20"), "http://google.com", "10.67.32.106", None, 104)
  }
}
