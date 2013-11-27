package ru.zconstz.shortener

import org.specs2.mutable.Specification
import scala.slick.session.{Session, Database}
import ru.zconstz.shortener.DataBaseEntities._

import scala.slick.driver.PostgresDriver.simple._
import com.typesafe.config.{Config, ConfigFactory}

class DataBaseEntitiesSpec extends Specification {

  "DatabaseEntities" should {

    "create functional DDL and fill it with correct test data" in {
      val conf:Config  = ConfigFactory.load()
      val dataBase = Database.forURL(conf.getString("database.url"),
        driver = conf.getString("database.driver"),
        user = conf.getString("database.user"),
        password = conf.getString("database.password"))

      dataBase.withSession {
        implicit session: Session =>
          DataBaseEntities.reCreateSchema()
          DataBaseEntities.insertTestData()

          val joinAll = for {
            click <- Clicks
            link <- click.link
            folder <- link.folder
            user <- folder.user
          } yield (user.token, folder.title, link.url, click.date)

          Query(Users.length).first mustEqual 2
          Query(Folders.length).first mustEqual 4
          Query(Links.length).first mustEqual 6
          Query(Clicks.length).first mustEqual 6
          joinAll.list().length mustEqual 4
      }
    }
  }
}
