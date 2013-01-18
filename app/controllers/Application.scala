package controllers

import play.api._
import play.api.mvc._
import models._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index())
  }
  
  def about = Action {
    Ok(views.html.index())
  }
  
  def categories(category: String) = Action {
    
    val teams = Data.teams(category)
    val tableInfo = Data.tableInfos(category)
    
    Ok(views.html.categories(category)(teams.values.toSeq)(Nil)(tableInfo)(None))
    
  }
  
  def teamPlayers(category: String, team: String) = Action {
    
    val teams = Data.teams(category)
    val selectedTeam = teams(team)
    val tableInfo = Data.tableInfos(category)
    val players = selectedTeam.players
    
    Ok(views.html.categories(category)(teams.values.toSeq)(players)(TableInfo("International Team", "Players", "Twitter Name"))(Some(selectedTeam.widget)))
  }
  
  def hashtags = Action {
    Ok(views.html.index())
  }
  
  def suggestions = Action {
    Ok(views.html.index())
  }
  
}