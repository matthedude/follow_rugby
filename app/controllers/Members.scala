package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Members extends Controller with Secured {

  val MemberHome = Redirect(routes.Members.memberList(0, 2, ""))

  val memberForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "name" -> nonEmptyText,
      "twitterName" -> nonEmptyText)(Member.apply)(Member.unapply))

  def memberList(page: Int, orderBy: Int, filter: String) = IsAuthenticated { _ =>
    implicit request =>
      Ok(views.html.admin.members.memberList(
        Member.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
        orderBy, filter))
  }

  def createMember = IsAuthenticated { _ =>
    _ =>
      Ok(views.html.admin.members.createMember(memberForm))
  }

  def editMember(id: Int) = IsAuthenticated { _ =>
    _ =>
      Member.findById(id).map { member =>
        Ok(views.html.admin.members.editMember(id)(memberForm.fill(member)))
      }.getOrElse(NotFound)
  }

  def saveMember = IsAuthenticated { _ =>
    implicit request =>
      memberForm.bindFromRequest.fold(
        formWithErrors =>
          BadRequest(views.html.admin.members.createMember(formWithErrors)),
        member => {
          Member.create(member)
          MemberHome.flashing("success" -> "Member %s has been created".format(member.name))
        })
  }

  def updateMember(id: Int) = IsAuthenticated { _ =>
    implicit request =>
      memberForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.admin.members.editMember(id)(formWithErrors)),
        member => {
          Member.update(id, member)
          MemberHome.flashing("success" -> "Member %s has been updated".format(member.name))
        })
  }

  def deleteMember(id: Int) = IsAuthenticated { _ =>
    _ =>
      Member.delete(id)
      MemberHome.flashing("success" -> "Member has been deleted")
  }
}