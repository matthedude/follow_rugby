package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Administrator extends Controller with Secured {

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying ("Invalid email or password", result => result match {
        case (email, password) => User.authenticate(email, password).isDefined
      }))

  def login = Action { implicit request =>
    Ok(views.html.admin.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.login(formWithErrors)),
      user => Redirect(routes.Administrator.admin).withSession("email" -> user._1))
  }

  def logout = Action {
    Redirect(routes.Administrator.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  def admin = IsAuthenticated { _ =>
    _ =>
      Ok(views.html.admin.tables())
  }

}

trait Secured {

  private def username(request: RequestHeader) = request.session.get("email")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Administrator.login)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }

}