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
  
  val withTeamNames = {
  	Game.simple ~ str("team1Name") ~ str("team2Name") map {
  		case game~team1~team2=> (game, team1, team2)
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
  
  def findById(team1Id: Long, team2Id: Long):Option[Game] = {
    DB.withConnection { implicit connection =>
      
      val game = SQL(
        """
          select *
          from game 
          where team1_id = {team1Id}
      		and team2_id = {team2Id}
        """
      ).on(
      		'team1Id -> team1Id,
      		'team2Id -> team2Id
      		).as(Game.simple singleOpt)
      
      game
    }
  
  }
  
  def create(game: Game) = {
    DB.withConnection { implicit connection =>
        SQL("""
            insert into game (team1_id, team2_id, time, game_date) values (
              {team1Id}, {team2Id}, {time}, {gameDate}
            )
            """).on(
          'team1Id -> game.team1Id,
          'team2Id -> game.team2Id,
          'time -> game.time,
          'gameDate -> game.gameDate
        ).executeUpdate()
    }
  }
 
  def update(team1Id: Long, team2Id: Long, game: Game) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update game
          set time = {time}, game_date = {gameDate}
          where team1_id = {team1Id}
          and team2_id = {team2Id}
        """
      ).on(
        'team1Id -> game.team1Id,
        'team2Id -> game.team2Id,
        'time -> game.time,
        'gameDate -> game.gameDate      
      ).executeUpdate()
    }
  }
  
  def delete(team1Id: Long, team2Id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from game where team1_id = {team1_id} and team2_id = {team2_id}"
      		).on(
      	'team1Id -> team1Id,
        'team2Id -> team2Id).executeUpdate()
    }
  }
  
  /**
   * Return a page of (Computer,Company).
   *
   * @param page Page to display
   * @param pageSize Number of computers per page
   * @param orderBy Computer property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1): Page[(Game, Option[Team], Option[Team])] = {
    
    val offest = pageSize * page
    
    DB.withConnection { implicit connection =>
      
      val games = SQL(
        """
          select * from game
          order by {orderBy} 
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize, 
        'offset -> offest,
        'orderBy -> orderBy
      ).as(Game.simple *)
      
      val gamesWithNames = games map { game => (game, Team.findById(game.team1Id), Team.findById(game.team2Id))}
      val totalRows = SQL(
        """
          select count(*) from game
        """
      ).as(scalar[Long].single)

      Page(gamesWithNames, page, offest, totalRows)
      
    }
    
  }
  
}