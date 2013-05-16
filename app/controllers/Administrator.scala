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
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => User.authenticate(email, password).isDefined
    })
  )

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
      user => Redirect(routes.Administrator.admin).withSession("email" -> user._1)
    )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Administrator.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
  
  
  
  val WidgetHome = Redirect(routes.Administrator.widgetList(0, 2, ""))
  
  val widgetForm = Form(
    mapping(
      "id" -> longNumber,
      "twitterAccount" -> nonEmptyText,
      "url" -> nonEmptyText
    )(Widget.apply)(Widget.unapply)
  )
  
  def admin = IsAuthenticated { _ => _ =>
    Ok(views.html.admin.tables())
  }
  
  def widgetList(page: Int, orderBy: Int, filter: String) = IsAuthenticated { _ => implicit request =>
    Ok(views.html.admin.widgetList(
      Widget.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  def createWidget = IsAuthenticated { _ => _ =>
    Ok(views.html.admin.createWidget(widgetForm))
  }
  
  def editWidget(id: Long) = IsAuthenticated { _ => _ =>
    Widget.findById(id).map { widget =>
      Ok(views.html.admin.editWidget(id)(widgetForm.fill(widget)))
    } .getOrElse(NotFound)
  }
  
  def saveWidget = IsAuthenticated { _ => implicit request =>
    widgetForm.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(views.html.admin.createWidget(formWithErrors)),
      widget => {
        Widget.create(widget)
        WidgetHome.flashing("success" -> "Widget %s has been created".format(widget.id))
      }
    )
  }
  
  def updateWidget(id: Long) = IsAuthenticated { _ => implicit request =>
    widgetForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.editWidget(id)(formWithErrors)),
      widget => {
        println(widget)
        Widget.update(id, widget)
        WidgetHome.flashing("success" -> "Widget %s has been updated".format(widget.id))
      }
    )
  }
  
  def deleteWidget(id: Long) = IsAuthenticated { _ => _ =>
    Widget.delete(id)
    WidgetHome.flashing("success" -> "Widget has been deleted")
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