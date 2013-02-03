package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Team(id: Pk[Int] = NotAssigned, name: String, categoryId: Long, widgetId: Long)

object Team {

  
  val simple = {
    get[Pk[Int]]("team.id") ~
    get[String]("team.name") ~
    get[Int]("team.category_id") ~ 
    get[Long]("team.widget_id") map {
      case id~name~categoryId~widgetId => Team(id, name, categoryId, widgetId)
    }
  }
  
  def all:Seq[Team] = {
    DB.withConnection { implicit connection =>
      
      val teams = SQL(
        """
          select * from team
        """
      ).as(Team.simple *)
      
      teams
    }
  }
  
  def findByCategoryId(categoryId: Pk[Int]):Seq[Team] = {
    DB.withConnection { implicit connection =>
      
      val teams = SQL(
        """
          select * from team
          where team.category_id = {categoryId}
        """
      ).on('categoryId -> categoryId).as(Team.simple *)
      
      teams
    }
  }
  
}