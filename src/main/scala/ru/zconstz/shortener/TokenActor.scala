package ru.zconstz.shortener

import akka.actor.Actor

class TokenActor extends Actor {

  def receive = {
    case None => {}
  }
}
