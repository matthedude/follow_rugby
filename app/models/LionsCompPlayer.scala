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
		password: String,
		phone: String)
		
object LionsCompTeam {
	def entryToTeam(entry: CompEntry):LionsCompTeam = {
		val players = LionsCompPlayer.findByIds(entry.players)
		create(players, entry.name, entry.email, entry.password, entry.phone)
	}
	
	private def create(players: Seq[LionsCompPlayer], name: String, email: String, password: String, phone: String) = {
		val props = players.filter(_.position == "Prop").map(_.id.get.toString).toList
		val hookers = players.filter(_.position == "Hooker").map(_.id.get.toString).toList
		val locks = players.filter(_.position == "Lock").map(_.id.get.toString).toList
		val backRows = players.filter(_.position == "Back Row").map(_.id.get.toString).toList
		val scrumHalfs = players.filter(_.position == "Scrum Half").map(_.id.get.toString).toList
		val flyHalfs = players.filter(_.position == "Fly Half").map(_.id.get.toString).toList
		val centres = players.filter(_.position == "Centre").map(_.id.get.toString).toList
		val wings = players.filter(_.position == "Wing").map(_.id.get.toString).toList
		val fullBacks = players.filter(_.position == "Full Back").map(_.id.get.toString).toList
		
		LionsCompTeam(
    props, 
    hookers, 
    locks, 
    backRows, 
    scrumHalfs, 
    flyHalfs, 
    centres, 
    wings, 
    fullBacks,
    name,
    email,
    password,
    phone)
	}
}


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
  
  def findByIds(ids: String):Seq[LionsCompPlayer] = {
    	// Split up the comma separated values
    val sids = ids split ","
    // Create a list of keys (id0, id1, id2, ...)
    val keys = for ( i <- 0 until sids.size ) yield ("id" + i)
    // Create a seq of parameterized values
  
   
    val values = sids map (toParameterValue(_))

    // Now zip together the keys and values into list of tuples
    val params = keys zip values
    
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
          from comp_player 
          where id in ({%s})
        """.format(keys.mkString("},{"))
      ).on(
        params: _*
      ).as(
        LionsCompPlayer.simple *
      )
    }
  }
  
  def findNotByIds(ids: String):Seq[LionsCompPlayer] = {
      // Split up the comma separated values
    val sids = ids split ","
    // Create a list of keys (id0, id1, id2, ...)
    val keys = for ( i <- 0 until sids.size ) yield ("id" + i)
    // Create a seq of parameterized values
  
   
    val values = sids map (toParameterValue(_))

    // Now zip together the keys and values into list of tuples
    val params = keys zip values
    
    DB.withConnection { implicit connection =>
      SQL(
        """
          select *
          from comp_player 
          where id not in ({%s})
        """.format(keys.mkString("},{"))
      ).on(
        params: _*
      ).as(
        LionsCompPlayer.simple *
      )
    }
  
  }
}