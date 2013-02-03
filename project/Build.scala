import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "follow_rugby"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "mysql" % "mysql-connector-java" % "5.1.20"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here  
      resourceDirectory in Compile <<= baseDirectory / "/app/resources/rugby"
    )

}
