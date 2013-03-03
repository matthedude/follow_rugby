package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Administrator extends Controller {
  import Data._
  
  val MemberHome = Redirect(routes.Administrator.memberList(0, 2, ""))
  val TeamHome = Redirect(routes.Administrator.teamList(0, 2, ""))
  val WidgetHome = Redirect(routes.Administrator.widgetList(0, 2, ""))
  val GameHome = Redirect(routes.Administrator.gameList(0, 2))
  
  val memberForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Int]),
      "name" -> nonEmptyText,
      "twitterName" -> nonEmptyText
    )(Member.apply)(Member.unapply)
  )
  
  val teamForm = Form(
    mapping(
      "team" -> mapping(
        "id" -> ignored(NotAssigned:Pk[Int]),
        "name" -> nonEmptyText,
        "categoryId" -> number,
        "widgetId" -> longNumber
      )(Team.apply)(Team.unapply),
      "members" -> list(number))
    (TeamMembers.apply)(TeamMembers.unapply)
  )
  
  val widgetForm = Form(
    mapping(
      "id" -> longNumber,
      "twitterAccount" -> nonEmptyText,
      "url" -> nonEmptyText
    )(Widget.apply)(Widget.unapply)
  )
  
  val gameForm = Form(
    mapping(
      "team1Id" -> number,
      "team2Id" -> number,
      "time" -> nonEmptyText,
      "gameDate"  -> date("yyyy-MM-dd")
    )(Game.apply)(Game.unapply)
  )
  
  def admin = Action {
    Ok(views.html.admin.tables())
  }
  
  def memberList(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(views.html.admin.memberList(
      Member.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  def createMember = Action {
    Ok(views.html.admin.createMember(memberForm))
  }
  
  def editMember(id: Int) = Action {
    Member.findById(id).map { member =>
      Ok(views.html.admin.editMember(id)(memberForm.fill(member)))
    } .getOrElse(NotFound)
  }
  
  def saveMember = Action { implicit request =>
    memberForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.admin.createMember(formWithErrors)),
      member => {
        Member.create(member)
        MemberHome.flashing("success" -> "Member %s has been created".format(member.name))
      }
    )
  }
  
  def updateMember(id: Int) = Action { implicit request =>
    memberForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.editMember(id)(formWithErrors)),
      member => {
      	println(member)
        Member.update(id, member)
        MemberHome.flashing("success" -> "Member %s has been updated".format(member.name))
      }
    )
  }
  
  def deleteMember(id: Int) = Action {
    Member.delete(id)
    MemberHome.flashing("success" -> "Member has been deleted")
  }
  
  def createTeam = Action {
    Ok(views.html.admin.createTeam(teamForm)(Category.all.map(c => (c.id.get.toString, c.name)))(Member.all))
  }
  
  def teamList(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
  Ok(views.html.admin.teamList(
      Team.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
      ))
  }
  
  def editTeam(id: Int) = Action {
    Team.findById(id).map { team =>
    	val membersIds = Team.findMembersById(id)
    	val members = membersIds.map(Member.findById).flatten
    	val teamMembers = TeamMembers(team, membersIds.toList)
      Ok(views.html.admin.editTeam(id)(teamForm.fill(teamMembers))(Category.all.map(c => (c.id.get.toString, c.name)))(members)(Member.all))
    } .getOrElse(NotFound)
  }
  
  def updateTeam(id: Int) = Action { implicit request =>
  	val membersIds = Team.findMembersById(id)
    val members = membersIds.map(Member.findById).flatten
    teamForm.bindFromRequest.fold(
      formWithErrors => 
      	BadRequest(views.html.admin.editTeam(id)(formWithErrors)(Category.all.map(c => (c.id.get.toString, c.name)))(members)(Member.all)),
      team => {
        Team.update(id, team.team)
        Team.updateTeamMembers(id, team.members.toSet)
        TeamHome.flashing("success" -> "Team %s has been updated".format(team.team.name))
      }
    )
  }
  
  def saveTeam = Action { implicit request =>
    teamForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.admin.createTeam(formWithErrors)(Category.all.map(c => (c.id.get.toString, c.name)))(Member.all)),
      team => {
        val id = Team.create(team.team)
        Team.updateTeamMembers(id, team.members.toSet)
        TeamHome.flashing("success" -> "Team %s has been created".format(team.team.name))
      }
    )
  }
  
  def deleteTeam(id: Int) = Action {
    Team.delete(id)
    TeamHome.flashing("success" -> "Team has been deleted")
  }
  
  def widgetList(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(views.html.admin.widgetList(
      Widget.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  def createWidget = Action {
    Ok(views.html.admin.createWidget(widgetForm))
  }
  
  def editWidget(id: Long) = Action {
    Widget.findById(id).map { widget =>
      Ok(views.html.admin.editWidget(id)(widgetForm.fill(widget)))
    } .getOrElse(NotFound)
  }
  
  def saveWidget = Action { implicit request =>
    widgetForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.admin.createWidget(formWithErrors)),
      widget => {
        Widget.create(widget)
        WidgetHome.flashing("success" -> "Widget %s has been created".format(widget.id))
      }
    )
  }
  
  def updateWidget(id: Long) = Action { implicit request =>
    widgetForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.editWidget(id)(formWithErrors)),
      widget => {
        println(widget)
        Widget.update(id, widget)
        WidgetHome.flashing("success" -> "Widget %s has been updated".format(widget.id))
      }
    )
  }
  
  def deleteWidget(id: Long) = Action {
    Widget.delete(id)
    WidgetHome.flashing("success" -> "Widget has been deleted")
  }
  
  def gameList(page: Int, orderBy: Int) = Action { implicit request =>
    Ok(views.html.admin.gameList(
      Game.list(page = page, orderBy = orderBy),
      orderBy
    ))
  }
  
  def createGame = Action {
    Ok(views.html.admin.createGame(gameForm)(Team.all map (t => (t.id.get.toString, t.name) )))
  }
  
  def editGame(team1Id: Int, team2Id: Int) = Action {
    Game.findById(team1Id, team2Id).map { game =>
    	val team1 = Team.findById(team1Id).get
    	val team2 = Team.findById(team2Id).get
      Ok(views.html.admin.editGame(gameForm.fill(game))(team1)(team2)(Team.all map (t => (t.id.get.toString, t.name) )))
    } .getOrElse(NotFound)
  }
  
  def saveGame = Action { implicit request =>
    gameForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.admin.createGame(formWithErrors)(Team.all map (t => (t.id.get.toString, t.name) ))),
      game => {
        Game.create(game)
        GameHome.flashing("success" -> "Game has been created")
      }
    )
  }
  
  def updateGame(team1Id: Int, team2Id: Int) = Action { implicit request =>
    gameForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.editGame(formWithErrors)(Team.findById(team1Id).get)(Team.findById(team2Id).get)(Team.all map (t => (t.id.get.toString, t.name) ))),
      game => {
        Game.update(team1Id, team2Id, game)
        GameHome.flashing("success" -> "Game has been updated")
      }
    )
  }
  
  def deleteGame(team1Id: Int, team2Id: Int) = Action {
    Game.delete(team1Id, team2Id)
    GameHome.flashing("success" -> "Game has been deleted")
  }
  
}