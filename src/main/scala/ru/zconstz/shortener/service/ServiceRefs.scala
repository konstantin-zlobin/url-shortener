package ru.zconstz.shortener.service

import akka.actor.{ActorRef, Props}
import ru.zconstz.shortener.Boot


trait ServiceRefs {
  lazy val tokenActor: ActorRef = Boot.system.actorOf(Props[TokenActor])
  lazy val linkActor: ActorRef = Boot.system.actorOf(Props[LinkActor])
  lazy val clickActor: ActorRef = Boot.system.actorOf(Props[ClickActor])
  lazy val folderActor: ActorRef = Boot.system.actorOf(Props[FolderActor])
}
