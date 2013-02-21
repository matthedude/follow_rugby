package controllers

import play.api._
import play.api.mvc._
import models._

object Administrator extends Controller {
  import Data._
  
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
    Ok(views.html.admin.tables())
  }
  
  def editMember(id: Long) = Action {
    Ok(views.html.admin.tables())
  }
}