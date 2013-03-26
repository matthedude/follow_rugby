package controllers

import play.api._
import play.api.mvc._
import models._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index())
  }
  
  def about = Action {
    Ok(views.html.about())
  }
  
  def matchCentre(id: Int) = Action {
    Ok(views.html.matchCentre(Game.findByCompetitionId(id)))
  }
  
  def selectCategory(id: Int) = Action {
    val category = Category.findById(id).get
    val teams = Team.findByCategoryId(id)
    val widget = category.widgetId map (wId => Widget.findById(wId).get)
    
    Ok(views.html.categories(category)(teams)(None)(Nil)(widget))
  }
  
  def selectTeam(categoryId: Int, teamId: Int) = Action {
    val category = Category.findById(categoryId).get
    val teams = Team.findByCategoryId(categoryId)
    val selectedTeam = Team.findById(teamId)
    val members = Member.findByTeamId(teamId)
    
    val widget = Widget.findById(selectedTeam.get.widgetId)
    
    Ok(views.html.categories(category)(teams)(selectedTeam)(members)(widget))
  }
  
  def hashtags = Action {
    Ok(views.html.index())
  }
  
  def suggestions = Action {
    Ok(views.html.index())
  }
  
}