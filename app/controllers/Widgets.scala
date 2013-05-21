package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Widgets extends Controller with Secured {

	val WidgetHome = Redirect(routes.Widgets.widgetList(0, 2, ""))

	val widgetForm = Form(
		mapping(
			"id" -> longNumber,
			"twitterAccount" -> nonEmptyText,
			"url" -> nonEmptyText)(Widget.apply)(Widget.unapply))

	def widgetList(page: Int, orderBy: Int, filter: String) = IsAuthenticated { _ =>
		implicit request =>
			Ok(views.html.admin.widgets.widgetList(
				Widget.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
				orderBy, filter))
	}

	def createWidget = IsAuthenticated { _ =>
		_ =>
			Ok(views.html.admin.widgets.createWidget(widgetForm))
	}

	def editWidget(id: Long) = IsAuthenticated { _ =>
		_ =>
			Widget.findById(id).map { widget =>
				Ok(views.html.admin.widgets.editWidget(id)(widgetForm.fill(widget)))
			}.getOrElse(NotFound)
	}

	def saveWidget = IsAuthenticated { _ =>
		implicit request =>
			widgetForm.bindFromRequest.fold(
				formWithErrors =>
					BadRequest(views.html.admin.widgets.createWidget(formWithErrors)),
				widget => {
					Widget.create(widget)
					WidgetHome.flashing("success" -> "Widget %s has been created".format(widget.id))
				})
	}

	def updateWidget(id: Long) = IsAuthenticated { _ =>
		implicit request =>
			widgetForm.bindFromRequest.fold(
				formWithErrors => BadRequest(views.html.admin.widgets.editWidget(id)(formWithErrors)),
				widget => {
					println(widget)
					Widget.update(id, widget)
					WidgetHome.flashing("success" -> "Widget %s has been updated".format(widget.id))
				})
	}

	def deleteWidget(id: Long) = IsAuthenticated { _ =>
		_ =>
			Widget.delete(id)
			WidgetHome.flashing("success" -> "Widget has been deleted")
	}

}