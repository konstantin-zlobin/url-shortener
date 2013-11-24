package ru.zconstz.shortener

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import ru.zconstz.shortener.HttpEntities.{FolderByIdGetRequest, FolderGetRequest, LinkByCodeGetRequest}

class UrlShortenerSpec extends Specification with Specs2RouteTest with UrlShortenerService {
  def actorRefFactory = system

  "MyService" should {

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
          """{"token":"222", "url":"http://google.com", "code":"123", "folder_id":"55"}""")) ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }

    "POST /link/123" in {
      Post("/link/123", HttpEntity(MediaTypes.`application/json`,
        """{"referrer":"jsfkje", "remote_ip":"11:12:13:14", "other_stats": "other..."}""")) ~> myRoute ~> check {
        status === OK
        handled must beTrue
        responseAs[String] must not be empty
      }
    }
  }
}