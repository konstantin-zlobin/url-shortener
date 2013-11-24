name := """url-shortener"""

version := "1.0"

scalaVersion := "2.10.3"

sbtVersion := "0.13"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

libraryDependencies ++= {
    val akkaV = "2.2.3"
    val sprayV = "1.2-RC3"
    Seq(
        "io.spray"            %   "spray-can"     % sprayV,
        "io.spray"            %   "spray-routing" % sprayV,
        "io.spray"            %%  "spray-json"    % "1.2.3",
        "io.spray"            %   "spray-testkit" % sprayV,
        "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
        "com.typesafe.akka"   %%  "akka-testkit"  % akkaV,
        "com.typesafe.slick"  %%  "slick"         % "1.0.0",
        "org.slf4j"           %   "slf4j-nop"     % "1.6.4",
        "postgresql"          %   "postgresql"    % "9.1-901.jdbc4",
        "org.specs2"          %%  "specs2"        % "2.2.3" % "test")
  }

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

seq(Revolver.settings: _*)
