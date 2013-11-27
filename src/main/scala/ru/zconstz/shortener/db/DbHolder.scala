package ru.zconstz.shortener.db

import com.typesafe.config.{ConfigFactory, Config}
import scala.slick.session.Database

object DbHolder {
  private val conf:Config  = ConfigFactory.load()
  val defaultLimit = 10
  val dataBase = Database.forURL(conf.getString("database.url"),
    driver = conf.getString("database.driver"),
    user = conf.getString("database.user"),
    password = conf.getString("database.password"))
}
