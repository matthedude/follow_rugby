package controllers

import play.api._
import play.api.mvc._
import models._

object Application extends Controller {
  
  import Data._
  
  def index = Action {
    Ok(views.html.index())
  }
  
  def about = Action {
    Ok(views.html.index())
  }
  
  def matchCentre = Action {
    Ok(views.html.matchCentre(Data.matches))
  }
  
  def selectCategory(id: Int) = Action {
    val category = idToCategories(id)
    val teams = categoriesToTeams(category)
    val widget = category.widgetId map (wId => idToWidgets(wId))
    
    Ok(views.html.categories(category)(teams)(Nil)(widget))
  }
  
  def selectTeam(categoryId: Int, teamId: Int) = Action {
    val category = idToCategories(categoryId)
    val teams = categoriesToTeams(category)
    val selectedTeam = idToTeams(teamId)
    val members = teamsToMembers(selectedTeam)
    
    val widget = idToWidgets(selectedTeam.widgetId)
    
    Ok(views.html.categories(category)(teams)(members)(Some(widget)))
  }
  
  def hashtags = Action {
    Ok(views.html.index())
  }
  
  def suggestions = Action {
    Ok(views.html.index())
  }
  
}