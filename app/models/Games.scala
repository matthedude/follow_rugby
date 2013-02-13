package models

import java.util.{Date}

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Game(team1Id: Int, team2Id: Int, time: String, gameDate: Date)


object Game {

  
  val simple = {
    get[Int]("game.team1_id") ~
    get[Int]("game.team2_id") ~
    get[String]("game.time") ~
    get[Date]("game.game_date") map {
      case team1Id~team2Id~time~gameDate => Game(team1Id, team2Id, time, gameDate)
    }
  }
  
  def all:Seq[Game] = {
    DB.withConnection { implicit connection =>
      
      val games = SQL(
        """
          select * from game
        """
      ).as(Game.simple *)
      
      games
    }
  }
  
}