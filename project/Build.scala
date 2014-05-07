import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "demo1"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
     "org.jsoup" % "jsoup" % "1.7.3",
    "javax.mail" % "mail" % "1.4",
    "gui.ava" % "html2image" % "0.9",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "com.clever-age" % "play2-elasticsearch" % "0.7-SNAPSHOT",
    "oro" % "oro" % "2.0.8",
    "commons-validator" % "commons-validator" % "1.3.1",
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
  	resolvers += Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
  	resolvers += "AOL yoava" at "http://repo.jfrog.org/artifactory/libs-releases"
  )

}
