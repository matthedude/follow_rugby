package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import anorm._
import play.api.templates.Html

object Games extends Controller with Secured {

  val GameHome = Redirect(routes.Games.gameList(0, 2))

  val gameForm = Form(
    mapping(
      "team1Id" -> number,
      "team2Id" -> number,
      "competitionId" -> number,
      "time" -> nonEmptyText,
      "gameDate" -> date("yyyy-MM-dd"),
      "widgetId" -> longNumber)(Game.apply)(Game.unapply))

  def gameList(page: Int, orderBy: Int) = IsAuthenticated { _ =>
    implicit request =>
      Ok(views.html.admin.games.gameList(
        Game.list(page = page, orderBy = orderBy),
        orderBy))
  }

  def createGame = IsAuthenticated { _ =>
    _ =>
      Ok(views.html.admin.games.createGame(gameForm)(Team.all map (t => (t.id.get.toString, t.name)))(Competition.all map (c => (c.id.get.toString, c.name))))
  }

  def editGame(team1Id: Int, team2Id: Int) = IsAuthenticated { _ =>
    _ =>
      Game.findById(team1Id, team2Id).map { game =>
        val team1 = Team.findById(team1Id)
        val team2 = Team.findById(team2Id)

        {
          for {
            t1 <- team1
            t2 <- team2
          } yield {
            Ok(views.html.admin.games.editGame(gameForm.fill(game))(t1)(t2)(Team.all map (t => (t.id.get.toString, t.name)))(Competition.all map (c => (c.id.get.toString, c.name))))
          }
        } getOrElse NotFound
      } getOrElse NotFound 
  }

  def saveGame = IsAuthenticated { _ =>
    implicit request =>
      gameForm.bindFromRequest.fold(
        formWithErrors =>
          BadRequest(views.html.admin.games.createGame(formWithErrors)(Team.all map (t => (t.id.get.toString, t.name)))(Competition.all map (c => (c.id.get.toString, c.name)))),
        game => {
          Game.create(game)
          GameHome.flashing("success" -> "Game has been created")
        })
  }

  def updateGame(team1Id: Int, team2Id: Int) = IsAuthenticated { _ =>
    implicit request =>
      gameForm.bindFromRequest.fold(
        formWithErrors => {
          val team1 = Team.findById(team1Id)
          val team2 = Team.findById(team2Id)

          {
            for {
              t1 <- team1
              t2 <- team2
            } yield {
              BadRequest(views.html.admin.games.editGame(formWithErrors)(t1)(t2)(Team.all map (t => (t.id.get.toString, t.name)))(Competition.all map (c => (c.id.get.toString, c.name))))
            }
          } getOrElse NotFound
        },
        game => {
          Game.update(team1Id, team2Id, game)
          GameHome.flashing("success" -> "Game has been updated")
        })
  }

  def deleteGame(team1Id: Int, team2Id: Int) = IsAuthenticated { _ =>
    _ =>
      Game.delete(team1Id, team2Id)
      GameHome.flashing("success" -> "Game has been deleted")
  }
}