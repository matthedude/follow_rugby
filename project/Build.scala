import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "follow_rugby"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "mysql" % "mysql-connector-java" % "5.1.20", anorm, jdbc
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here  
      resourceDirectory in Compile <<= baseDirectory / "/app/resources/rugby"
    )

}
