package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import anorm.Pk

object Application extends Controller {

  val competitionForm = Form(
    single(
      "compId" -> number))

  val Home = Redirect(routes.Application.index)

  def index = Action { implicit request =>
    Ok(views.html.index(Video.allWithVideoCategoryPlayerLatest.map{case(v, vc, vp) => (v, vc, vp, VideoHtml(v, vp))}, Game.allGamesWithComp.groupBy(_._2).map { case (c, v) => (c, v.map(_._1)) }, Member.latestMembersWithTeamCategory))
  }

  def about = Action {
    Ok(views.html.about())
  }

  def joinIn = Action {
    Ok(views.html.joinIn())
  }

  def matchCentre(id: Int, categoryName: String, t1Id: Int, t2Id: Int) = Action {
    val game = if (t1Id == 0 || t2Id == 0) {
      None
    } else {
      for {
        team1 <- Team.findById(t1Id)
        team2 <- Team.findById(t2Id)
        game <- Game.findById(t1Id, t2Id)
      } yield Game.matchCentreGame(team1, team2, game)
    }

    Ok(views.html.matchCentre(Game.findByCompetitionId(id), Competition.findById(id), game))
  }

  def selectCategory(id: Int, categoryName: String) = Action {
    val category = Category.findById(id)
    val teams = Team.findByCategoryId(id)
    val widget = category flatMap (_.widgetId flatMap (wId => Widget.findById(wId)))

    //god-awful hack will fix when I have time, please don't judge me!!!
    val media = {
      if (id == 10) Media(None, widget, None) else Media(widget, None, None)

    }

    {
      for {
        c <- category
      } yield {
        Ok(views.html.categories(c)(teams)(None)(Nil)(media))
      }
    } getOrElse NotFound

  }

  def selectTeam(categoryId: Int, teamId: Int, categoryName: String, teamName: String) = Action {
    val category = Category.findById(categoryId)
    val teams = Team.findByCategoryId(categoryId)
    val selectedTeam = Team.findById(teamId)
    val members = Member.findByTeamId(teamId)
    val video = selectedTeam.flatMap { Team.withVideo(_) }

    val widget = selectedTeam flatMap { t =>
      Widget.findById(t.widgetId)
    }
    category map { c =>
      Ok(views.html.categories(c)(teams)(selectedTeam)(members)(Media(widget, None, video)))
    } getOrElse NotFound
  }

  def selectVideoCategory(id: Int, videoCategoryName: String, page: Int = 0, filter: String = "", competitionId: Int = 0) = Action { implicit request =>

    VideoCategory.findById(id) map { videoCategory =>

      if (videoCategory.name == "Highlights") {

        competitionForm.bindFromRequest.fold(
          formWithErrors => {
            Ok(views.html.highlights(Video.listForVideoCategory(page = page, filter = ("%" + filter + "%"), videoCategoryId = id), filter, videoCategory, Competition.allForForm, competitionForm.fill(competitionId), competitionId))
          },
          compId => {
            Competition.findById(compId) map { competition =>
            Ok(views.html.highlights(Video.listForVideoCategory(page = page, filter = ("%" + competition.name + "%"), videoCategoryId = id), competition.name, videoCategory, Competition.allForForm, competitionForm.fill(compId), compId))
            } getOrElse NotFound
           })

      } else {
        Ok(views.html.videoCategories(Video.listWithVideoPlayer(page = page, filter = ("%" + filter + "%"), videoCategoryId = id), filter, videoCategory))
      }
    } getOrElse NotFound
  }

  def selectVideo(id: Int, videoCategoryId: Int, videoCategoryName: String, description: String) = Action {
    {
      for {
        video <- Video.findById(id)
        videoPlayer <- VideoPlayer.findById(video.videoPlayerId)
        videoCategory <- VideoCategory.findById(videoCategoryId)
        videoHtml = VideoHtml(video, videoPlayer)
        openGraph = OpenGraph(video.title, videoHtml.largeThumbnail, video.title)
      } yield {
        Ok(views.html.videos(video, videoCategory, videoPlayer, Video.previousForVideoCategoryWithPlayer(id, videoCategoryId), videoHtml, openGraph))
      }
    } getOrElse NotFound
  }

  def search(filter: String, startMember: Int, startTeam: Int) = Action {
    val results = if (filter.isEmpty) SearchResults() else Member.search("%" + filter + "%")
    Ok(views.html.search(results, filter, startMember, startTeam))
  }

  def comingSoon = Action {
    Ok(views.html.comingSoon())
  }

}