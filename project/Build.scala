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
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "AOL yoava" at "http://repo.jfrog.org/artifactory/libs-releases"
  )

}
