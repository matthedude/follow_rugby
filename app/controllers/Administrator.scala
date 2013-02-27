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
  
  def admin = Action {
    Ok(views.html.admin.tables())
  }
  
  def memberList(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(views.html.admin.memberList(
      Member.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  def teamList(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(views.html.admin.teamList(
      Team.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  def createMember = Action {
    Ok(views.html.admin.tables())
  }
  
  def createTeam = Action {
    Ok(views.html.admin.tables())
  }
  
  def editMember(id: Int) = Action {
    Member.findById(id).map { member =>
      Ok(views.html.admin.editMember(id)(memberForm.fill(member)))
    } .getOrElse(NotFound)
  }
  
  def updateMember(id: Int) = Action { implicit request =>
    memberForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.editMember(id)(formWithErrors)),
      member => {
        Member.update(id, member)
        MemberHome.flashing("success" -> "Member %s has been updated".format(member.name))
      }
    )
  }
  
  def deleteMember(id: Int) = Action {
    Member.delete(id)
    MemberHome.flashing("success" -> "Member has been deleted")
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
    println(request.body)
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
  
  def deleteTeam(id: Int) = Action {
    Team.delete(id)
    TeamHome.flashing("success" -> "Team has been deleted")
  }
  
}