package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Competition(id: Pk[Int] = NotAssigned, name: String, pos: Int)

object Competition {

	val simple = {
    get[Pk[Int]]("competition.id") ~
    get[String]("competition.name") ~
    get[Int]("competition.pos") map {
      case id~name~pos => Competition(id, name, pos)
    }
  }
  
  def all:Seq[Competition] = {
    DB.withConnection { implicit connection =>
      
      val competitions = SQL(
        """
          select * from competition
        """
      ).as(Competition.simple *)
      
      competitions
    }
  }
  
  def findById(id: Int):Option[Competition] = {
    DB.withConnection { implicit connection =>
      
      val competition = SQL(
        """
          select *
          from competition 
          where id = {id}
        """
      ).on('id -> id).as(Competition.simple singleOpt)
      
      competition
    }
  
  }
}