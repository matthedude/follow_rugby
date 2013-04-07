package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Application extends Controller {
  
	val Home = Redirect(routes.Application.index)
	
	val lionsCompForm = Form(
    (mapping(
      "props" -> list(text),
      "hookers" -> list(text),
      "locks" -> list(text),
      "backRows" -> list(text),
      "scrumHalfs" -> list(text),
      "flyHalfs" -> list(text),
      "centres" -> list(text),
      "wings" -> list(text),
      "fullBacks" -> list(text),
      "name" -> nonEmptyText,
      "email" -> (nonEmptyText verifying email.constraints.head),
      "phone" -> nonEmptyText
    )(LionsCompTeam.apply)(LionsCompTeam.unapply)
  ).verifying("A person with this email has already entered the competition.", entry => CompEntry.findByEmail(entry.email) == None))
  
  def index = Action { implicit request =>
    Ok(views.html.index())
  }
	
	def enterCompetition = Action { implicit request =>
		println(request)
    lionsCompForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.competition(LionsCompPlayer.all)(formWithErrors.withGlobalError("You have made an error filling out the form. Please make sure you have supplied your name, a valid email and a telephone number."))),
      compEntry => {
        val players = (compEntry.props ++ compEntry.hookers ++ compEntry.locks ++ compEntry.backRows ++ compEntry.scrumHalfs ++ compEntry.flyHalfs ++ compEntry.centres ++ compEntry.wings ++ compEntry.fullBacks).mkString(",")
        CompEntry.create(CompEntry(compEntry.name, compEntry.email, compEntry.phone, players))
        Home.flashing("success" -> "You have successfully entered the Follow Rugby Lions competition. Please check your email shortly for a confirmation email.")
//        Home
      }
    )
  }
	
  
  def about = Action {
    Ok(views.html.about())
  }
  
  def matchCentre(id: Int) = Action {
    Ok(views.html.matchCentre(Game.findByCompetitionId(id))(Competition.findById(id)))
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
  
  def comingSoon = Action {
    Ok(views.html.comingSoon())
  }
  
  def competition = Action { implicit request =>
  	Ok(views.html.competition(LionsCompPlayer.all)(lionsCompForm))
  }
  
  
  
}