package models

import play.api.Play.current
import play.api.db._
import play.api.templates.Html

import anorm._
import anorm.SqlParser._
import play.api.Play

import controllers.Assets

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class TeamMembers(team: Team, members: List[Int])
case class OpenGraph(description: String, image: String, title: String) {
  val siteName = "Follow Rugby"
  val publisher = "www.facebook.com/FollowRugby"
  val ogType = "article"  
  val toHtml: Html = {
    val htmlStr = """
      <meta property="og:description" content="""" + description + """">
      <meta property="og:image" content="""" + image + """">
      <meta property="og:site_name" content="Follow Rugby">
      <meta property="og:title" content="""" + title + """">
      <meta property="og:type" content="""" + ogType + """">
      <meta property="og:site_name" content="""" + siteName + """">
      <meta property="article:publisher" content="""" + publisher + """">
      """
      Html(htmlStr)
  }
} 

object OpenGraph {
  val defaultImage = "http://www.follow-rugby.com/assets/images/fbpic.jpg"
  val default: OpenGraph = OpenGraph(
      "Follow Rugby is your one-stop shop for live Twitter match updates, great rugby videos from around the web and we've organised all the best rugby Twitter accounts for you to follow.",
      defaultImage,
      "Follow Rugby")
  val defaultMatchGame: OpenGraph = OpenGraph(
      "See live Twitter commentary on rugby matches in all major competitions.",
      defaultImage,
      "Follow Rugby")
      
  
}