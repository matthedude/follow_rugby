package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Widget(id: Pk[Long] = NotAssigned, twitterAccount: String, url: String)

object Widget {

  
  val simple = {
    get[Pk[Long]]("widget.id") ~
    get[String]("widget.twitter_account") ~
    get[String]("widget.url") map {
      case id~name~url=> Widget(id, name, url)
    }
  }
  
  def all:Seq[Widget] = {
    DB.withConnection { implicit connection =>
      
      val widgets = SQL(
        """
          select * from widget
        """
      ).as(Widget.simple *)
      
      widgets
    }
  }
  
}