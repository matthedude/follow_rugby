package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

import anorm._

object Application extends Controller {

	val Home = Redirect(routes.Application.index)

	def index = Action { implicit request =>
		Ok(views.html.index())
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

		val (widget1, searchWidget) = {
			if (id == 10) (None, widget) else (widget, None)
		}

		Ok(views.html.categories(category)(teams)(None)(Nil)(widget1)(searchWidget))
	}

	def selectTeam(categoryId: Int, teamId: Int, categoryName: String, teamName: String) = Action {
		val category = Category.findById(categoryId).get
		val teams = Team.findByCategoryId(categoryId)
		val selectedTeam = Team.findById(teamId)
		val members = Member.findByTeamId(teamId)

		val widget = Widget.findById(selectedTeam.get.widgetId)

		Ok(views.html.categories(category)(teams)(selectedTeam)(members)(widget)(None))
	}

	def comingSoon = Action {
		Ok(views.html.comingSoon())
	}
}