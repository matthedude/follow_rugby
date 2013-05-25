package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Application extends Controller {

  val Home = Redirect(routes.Application.index)

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def about = Action {
    Ok(views.html.about())
  }

  def matchCentre(id: Int, categoryName: String) = Action {
    Ok(views.html.matchCentre(Game.findByCompetitionId(id))(Competition.findById(id)))
  }

  def selectCategory(id: Int, categoryName: String) = Action {
    val category = Category.findById(id)
    val teams = Team.findByCategoryId(id)
    val widget = category map (_.widgetId map (wId => Widget.findById(wId)))

    //god-awful hack will fix when I have time, please don't judge me!!!
    val widgets = {
      widget map { w => 
      if (id == 10) (None, w) else (w, None)
      }
    }
    
    {for {
      c <- category
      ws <- widgets
      w1 <- ws._1
      w2 <- ws._2
    } yield { 
      Ok(views.html.categories(c)(teams)(None)(Nil)(w1)(w2))
    }} getOrElse NotFound
    
  }

  def selectTeam(categoryId: Int, teamId: Int, categoryName: String, teamName: String) = Action {
    val category = Category.findById(categoryId)
    val teams = Team.findByCategoryId(categoryId)
    val selectedTeam = Team.findById(teamId)
    val members = Member.findByTeamId(teamId)

    val widget = selectedTeam flatMap { t =>
      Widget.findById(t.widgetId)
    }
    category map { c =>
      Ok(views.html.categories(c)(teams)(selectedTeam)(members)(widget)(None))
    } getOrElse NotFound
  }

  def comingSoon = Action {
    Ok(views.html.comingSoon())
  }
}