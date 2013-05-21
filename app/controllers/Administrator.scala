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

	/**
	 * Login page.
	 */
	def login = Action { implicit request =>
		Ok(views.html.admin.login(loginForm))
	}

	/**
	 * Handle login form submission.
	 */
	def authenticate = Action { implicit request =>
		loginForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.admin.login(formWithErrors)),
			user => Redirect(routes.Administrator.admin).withSession("email" -> user._1))
	}

	/**
	 * Logout and clean the session.
	 */
	def logout = Action {
		Redirect(routes.Administrator.login).withNewSession.flashing(
			"success" -> "You've been logged out")
	}

	def admin = IsAuthenticated { _ =>
		_ =>
			Ok(views.html.admin.tables())
	}

}

/**
 * Provide security features
 */
trait Secured {

	/**
	 * Retrieve the connected user email.
	 */
	private def username(request: RequestHeader) = request.session.get("email")

	/**
	 * Redirect to login if the user in not authorized.
	 */
	private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Administrator.login)

	// --

	/**
	 * Action for authenticated users.
	 */
	def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
		Action(request => f(user)(request))
	}

}