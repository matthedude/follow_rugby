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
      "password" -> nonEmptyText,
      "phone" -> nonEmptyText
    )(LionsCompTeam.apply)(LionsCompTeam.unapply)
  ))
  
  val loadCompForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => CompEntry.loadEntry(email, password).isDefined
    })
  )
  
  def index = Action { implicit request =>
    Ok(views.html.index())
  }
	
	def enterCompetition = Action { implicit request =>
    lionsCompForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.competition(LionsCompPlayer.all)(Nil)(formWithErrors.withGlobalError("You have made an error filling out the form. Please make sure you have supplied your name, a valid email and a telephone number."))(loadCompForm)),
      compEntry => {
      	import java.util.Date
        val players = (compEntry.props ++ compEntry.hookers ++ compEntry.locks ++ compEntry.backRows ++ compEntry.scrumHalfs ++ compEntry.flyHalfs ++ compEntry.centres ++ compEntry.wings ++ compEntry.fullBacks).mkString(",")
      	val entry = CompEntry(compEntry.name, compEntry.email, compEntry.password, compEntry.phone, players)
      	val compSelectedPlayers = LionsCompPlayer.findByIds(players).map(_.name)
        if(CompEntry.findByEmail(compEntry.email) == None) {
          CompEntry.create(entry)
          Email.sendEnterConfirmationMail(compSelectedPlayers, compEntry)
      	} else {
      		val updated = CompEntry.update(entry)
      		if(!updated) {
      			Home.flashing("error" -> "There was a problem entering the competition, please try again. If you have a saved squad then re-load it.")
      		} else {
      			Email.sendUpdateConfirmationMail(compSelectedPlayers, compEntry)
      		}
      	}
        Home.flashing("success" -> "You have successfully entered the Follow Rugby Lions competition. Please check your email shortly for a confirmation email.")
      }
    )
  }
	
  
  def about = Action {
    Ok(views.html.about())
  }
  
  def matchCentre(id: Int, categoryName: String) = Action {
    Ok(views.html.matchCentre(Game.findByCompetitionId(id))(Competition.findById(id)))
  }
  
  def selectCategory(id: Int, categoryName: String) = Action {
    val category = Category.findById(id).get
    val teams = Team.findByCategoryId(id)
    val widget = category.widgetId map (wId => Widget.findById(wId).get)
    
    Ok(views.html.categories(category)(teams)(None)(Nil)(widget))
  }
  
  def selectTeam(categoryId: Int, teamId: Int, categoryName: String, teamName: String) = Action {
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
  	Ok(views.html.competition(LionsCompPlayer.all)(Nil)(lionsCompForm)(loadCompForm))
  }
  
  def loadCompetition = Action { implicit request =>
    loadCompForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.competition(LionsCompPlayer.all)(Nil)(lionsCompForm)(formWithErrors)),
      login => {
        val entry = CompEntry.loadEntry(login._1, login._2)
        if(entry.isDefined) {
          val compSelectedPlayers = LionsCompPlayer.findByIds(entry.get.players)
          val compOtherPlayers = LionsCompPlayer.findNotByIds(entry.get.players)
          Ok(views.html.competition(compOtherPlayers)(compSelectedPlayers)(lionsCompForm.fill(LionsCompTeam.entryToTeam(entry.get)))(loadCompForm))
        } else {
        	Ok(views.html.competition(LionsCompPlayer.all)(Nil)(lionsCompForm)(loadCompForm))
        }
      }
    )
  }
}