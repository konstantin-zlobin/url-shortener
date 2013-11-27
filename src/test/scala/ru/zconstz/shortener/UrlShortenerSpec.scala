package ru.zconstz.shortener

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import ru.zconstz.shortener.http.{UrlShortenerService, HttpEntities}
import ru.zconstz.shortener.http.HttpEntities._
import akka.actor.{Actor, Props, ActorRef}
import ru.zconstz.shortener.service.{LinkActor, TokenActor}
import ru.zconstz.shortener.http.HttpEntities.TokenGetRequest
import akka.testkit.TestActorRef

class TokenFakeActor extends Actor {
  def receive = {
    case TokenGetRequest(userId, secret) => sender ! TokenGetResponse("12345")
  }
}

class LinkFakeActor extends Actor {
  def receive = {
    case LinkGetRequest(token, offset, limit) => sender ! List(Link("http://google.com", "123DF34"))
    case LinkPostRequest(token, url, proposedCode, folder_id) => sender ! Right(Link("http://google.com", "123DF34"))
    case (code: String, LinkByCodeGetRequest(token)) =>
      sender ! Right(LinkByCodeGetResponse(Link("http://google.com", code), None, 100))
  }
}

class ClicksFakeActor extends Actor {
  def receive = {
    case (code: String, LinkByCodePostRequest(referer, remoteIp, otherStats)) =>
      sender ! Right(LinkByCodePostResponse("http://google.com"))
  }
}

class UrlShortenerSpec extends Specification with Specs2RouteTest with UrlShortenerService {

  override lazy val tokenActor: ActorRef = TestActorRef(Props[TokenFakeActor])
  override lazy val linkActor: ActorRef = TestActorRef(Props[LinkFakeActor])
  override lazy val clicksActor: ActorRef = TestActorRef(Props[ClicksFakeActor])

  def actorRefFactory = system

  "UrlShortenerService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> myRoute ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> myRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(myRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }

    "GET /token" in {
      Get("/token?user_id=12345&secret=09876") ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "GET /link" in {
      Get("/link?token=222&offset=1&limit=5") ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "GET /link/$code" in {
      Get("/link/123?token=222") ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "GET /link/$code/clicks" in {
      Get("/link/123/clicks?token=222&offset=1&limit=5") ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "GET /folder" in {
      Get("/folder?token=222") ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "GET /folder/$id" in {
      Get("/folder/123?token=222&offset=5&limit=10") ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "POST /link" in {
      Post("/link", HttpEntity(MediaTypes.`application/json`,
          """{"token":"222", "url":"http://google.com", "code":"123", "folder_id": 55}""")) ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "POST /link/123" in {
      Post("/link/123", HttpEntity(MediaTypes.`application/json`,
        """{"referer":"http://wwww.google.com", "remote_ip":"11:12:13:14", "other_stats": "other..."}""")) ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }
  }
}