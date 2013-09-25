package models

import java.util.{ Date }

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Game(team1Id: Int, team2Id: Int, competitionId: Int, time: String, gameDate: Date, widgetId: Long, pos: Int, hashtag: String)
case class MatchCentreGame(team1: Team, team2: Team, game: Game, team1Widget: Widget, team2Widget: Widget, matchWidget: Option[Widget])

object Game {

  val simple = {
    get[Int]("game.team1_id") ~
      get[Int]("game.team2_id") ~
      get[Int]("game.competition_id") ~
      get[String]("game.time") ~
      get[Date]("game.game_date") ~
      get[Long]("game.widget_id") ~
      get[Int]("game.pos") ~
      get[String]("game.hashtag") map {
        case team1Id ~ team2Id ~ competitionId ~ time ~ gameDate ~ widgetId ~ pos ~ hashtag => Game(team1Id, team2Id, competitionId, time, gameDate, widgetId, pos, hashtag)
      }
  }

  val withTeamNames = {
    Game.simple ~ str("team1Name") ~ str("team2Name") map {
      case game ~ team1 ~ team2 => (game, team1, team2)
    }
  }
  
  val withCompetitionName = {
    Game.simple ~ Competition.simple map {
      case game ~ comp => (game, comp)
    }
  }

  def all: Seq[Game] = {
    DB.withConnection { implicit connection =>

      val games = SQL(
        """
          select * from game
        """).as(Game.simple *)

      games
    }
  }

  def allMatches(games: Seq[Game]): Seq[MatchCentreGame] = {
    DB.withConnection { implicit connection =>
      games map { game =>
        val team1 = Team.findById(game.team1Id).get
        val team2 = Team.findById(game.team2Id).get
        matchCentreGame(team1, team2, game)
      }
    }
  }
  
  def matchCentreGame(team1: Team, team2: Team, game: Game) = {
    MatchCentreGame(team1, team2, game, Widget.findById(team1.widgetId).get, Widget.findById(team2.widgetId).get, Widget.findById(game.widgetId))
  }
  
  def allGamesWithComp = {
    DB.withConnection { implicit connection =>

      val games = SQL(
        """
          select * from game 
          left join competition
          on game.competition_id = competition.id
        """).as(Game.withCompetitionName *)

      games
    }
  }

  def findById(team1Id: Int, team2Id: Int): Option[Game] = {
    DB.withConnection { implicit connection =>

      val game = SQL(
        """
          select *
          from game 
          where team1_id = {team1Id}
      		and team2_id = {team2Id}
        """).on(
          'team1Id -> team1Id,
          'team2Id -> team2Id).as(Game.simple singleOpt)
          println(game)
      game
    }

  }

  def findByCompetitionId(competitionId: Int): Seq[MatchCentreGame] = {
    DB.withConnection { implicit connection =>

      val games = SQL(
        """
          select *
          from game 
          where competition_id = {competitionId}
        """).on(
          'competitionId -> competitionId).as(Game.simple *)

      allMatches(games)
    }

  }

  def create(game: Game) = {
    DB.withConnection { implicit connection =>
      SQL("""
            insert into game (team1_id, team2_id, competition_id, time, game_date, widget_id, pos, hashtag) values (
              {team1Id}, {team2Id}, {competitionId}, {time}, {gameDate}, {widgetId}, {pos}, {hashtag}
            )
            """).on(
        'team1Id -> game.team1Id,
        'team2Id -> game.team2Id,
        'competitionId -> game.competitionId,
        'time -> game.time,
        'gameDate -> game.gameDate,
        'widgetId -> game.widgetId,
        'pos -> game.pos,
        'hashtag -> game.hashtag).executeUpdate()
    }
  }

  def update(team1Id: Long, team2Id: Long, game: Game) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update game
          set time = {time}, game_date = {gameDate}, widget_id = {widgetId}, competition_id = {competitionId}, pos = {pos}, hashtag = {hashtag}
          where team1_id = {team1Id}
          and team2_id = {team2Id}
        """).on(
          'team1Id -> game.team1Id,
          'team2Id -> game.team2Id,
          'competitionId -> game.competitionId,
          'time -> game.time,
          'gameDate -> game.gameDate,
          'widgetId -> game.widgetId,
          'pos -> game.pos,
          'hashtag -> game.hashtag).executeUpdate()
    }
  }

  def delete(team1Id: Long, team2Id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from game where team1_id = {team1Id} and team2_id = {team2Id}").on(
        'team1Id -> team1Id,
        'team2Id -> team2Id).executeUpdate()
    }
  }
  
  def deleteAll = {
    DB.withConnection { implicit connection =>
      val games = all
      games foreach { game =>
        SQL("delete from widget where id={id}").on('id -> game.widgetId).executeUpdate()
      }
      SQL("delete from game").executeUpdate()
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
        """).on(
          'pageSize -> pageSize,
          'offset -> offest,
          'orderBy -> orderBy).as(Game.simple *)

      val gamesWithNames = games map { game => (game, Team.findById(game.team1Id), Team.findById(game.team2Id)) }
      val totalRows = SQL(
        """
          select count(*) from game
        """).as(scalar[Long].single)

      Page(gamesWithNames, page, offest, totalRows)

    }

  }

}