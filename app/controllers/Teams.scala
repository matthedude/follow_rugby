package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Teams extends Controller with Secured {

  val TeamHome = Redirect(routes.Teams.teamList(0, 2, ""))

  val teamForm = Form(
    mapping(
      "team" -> mapping(
        "id" -> ignored(NotAssigned: Pk[Int]),
        "name" -> nonEmptyText,
        "twitterName" -> optional(text),
        "categoryId" -> number,
        "widgetId" -> longNumber,
        "vidChannel" -> optional(text),
        "videoPlayer" -> optional(number),
        "hashtag" -> optional(text))(Team.apply)(Team.unapply),
      "members" -> list(number))(TeamMembers.apply)(TeamMembers.unapply))

  def createTeam = IsAuthenticated { _ =>
    _ =>
      Ok(views.html.admin.teams.createTeam(teamForm)(Category.all.map(c => (c.id.get.toString, c.name)))(Member.all)(VideoPlayer.allFormTuple))
  }

  def teamList(page: Int, orderBy: Int, filter: String) = IsAuthenticated { _ =>
    implicit request =>
      Ok(views.html.admin.teams.teamList(
        Team.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
        orderBy, filter))
  }

  def editTeam(id: Int) = IsAuthenticated { _ =>
    _ =>
      Team.findById(id).map { team =>
        val membersIds = Team.findMembersById(id)
        val members = membersIds.map(Member.findById).flatten
        val teamMembers = TeamMembers(team, membersIds.toList)
        Ok(views.html.admin.teams.editTeam(id)(teamForm.fill(teamMembers))(Category.all.map(c => (c.id.get.toString, c.name)))(members)(Member.all)(VideoPlayer.allFormTuple))
      }.getOrElse(NotFound)
  }

  def updateTeam(id: Int) = IsAuthenticated { _ =>
    implicit request =>
      val membersIds = Team.findMembersById(id)
      val members = membersIds.map(Member.findById).flatten
      teamForm.bindFromRequest.fold(
        formWithErrors =>
          BadRequest(views.html.admin.teams.editTeam(id)(formWithErrors)(Category.all.map(c => (c.id.get.toString, c.name)))(members)(Member.all)(VideoPlayer.allFormTuple)),
        team => {
          Team.update(id, team.team)
          Team.updateTeamMembers(id, team.members.toSet)
          TeamHome.flashing("success" -> "Team %s has been updated".format(team.team.name))
        })
  }

  def saveTeam = IsAuthenticated { _ =>
    implicit request =>
      teamForm.bindFromRequest.fold(
        formWithErrors =>
          BadRequest(views.html.admin.teams.createTeam(formWithErrors)(Category.all.map(c => (c.id.get.toString, c.name)))(Member.all)(VideoPlayer.allFormTuple)),
        team => {
          val id = Team.create(team.team)
          Team.updateTeamMembers(id, team.members.toSet)
          TeamHome.flashing("success" -> "Team %s has been created".format(team.team.name))
        })
  }

  def deleteTeam(id: Int) = IsAuthenticated { _ =>
    _ =>
      Team.delete(id)
      TeamHome.flashing("success" -> "Team has been deleted")
  }
}