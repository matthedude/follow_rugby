package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class LionsCompPlayer(id: Pk[Int] = NotAssigned, name: String, position: String)

case class LionsCompTeam(
		props: List[String], 
		hookers: List[String], 
		locks: List[String], 
		backRows: List[String], 
		scrumHalfs: List[String], 
		flyHalfs: List[String], 
		centres: List[String], 
		wings: List[String], 
		fullBacks: List[String],
		name: String,
		email: String,
		phone: String)


object LionsCompPlayer {
	
  val simple = {
    get[Pk[Int]]("comp_player.id") ~
    get[String]("comp_player.name") ~
    get[String]("comp_player.position") map {
      case id~name~pos => LionsCompPlayer(id, name, pos)
    }
  }
  
  def all:Seq[LionsCompPlayer] = {
    DB.withConnection { implicit connection =>
      
      val players = SQL(
        """
          select * from comp_player
        """
      ).as(LionsCompPlayer.simple *)
      
      players
    }
  }
  
  def findById(id: Int):Option[LionsCompPlayer] = {
    DB.withConnection { implicit connection =>
      
      val player = SQL(
        """
          select *
          from comp_player 
          where id = {id}
        """
      ).on('id -> id).as(LionsCompPlayer.simple singleOpt)
      
      player
    }
  
  }
}