package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import java.util.Date

import anorm._

object Videos extends Controller with Secured {

  val VideoHome = Redirect(routes.Videos.videoList(0, 2, ""))
//case class Video(id: Pk[Int] = NotAssigned, videoCategoryId: Int, videoPlayerId: Int, link: String, description: String, title: String, date: Date)

  val videoForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "videoCategoryId" -> number,
      "videoPlayerId" -> number,
      "link" -> nonEmptyText,
      "description" -> nonEmptyText,
      "title" -> nonEmptyText,
      "date" -> ignored(new Date),
      "thumbnailLink" -> optional(text))(Video.apply)(Video.unapply))

  def videoList(page: Int, orderBy: Int, filter: String) = IsAuthenticated { _ =>
    implicit request =>
      Ok(views.html.admin.videos.videoList(
        Video.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
        orderBy, filter))
  }
  

  def createVideo = IsAuthenticated { _ =>
    _ =>
      Ok(views.html.admin.videos.createVideo(videoForm)(VideoPlayer.allFormTuple)(VideoCategory.allFormTuple))
  }

  def editVideo(id: Int) = IsAuthenticated { _ =>
    _ =>
      Video.findById(id).map { video =>
        Ok(views.html.admin.videos.editVideo(id)(videoForm.fill(video))(VideoPlayer.allFormTuple)(VideoCategory.allFormTuple))
      }.getOrElse(NotFound)
  }

  def saveVideo = IsAuthenticated { _ =>
    implicit request =>
      videoForm.bindFromRequest.fold(
        formWithErrors =>
          BadRequest(views.html.admin.videos.createVideo(formWithErrors)(VideoPlayer.allFormTuple)(VideoCategory.allFormTuple)),
        video => {
          Video.create(video)
          VideoHome.flashing("success" -> "Video [%s] has been created".format(video.title))
        })
  }

  def updateVideo(id: Int) = IsAuthenticated { _ =>
    implicit request =>
      videoForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.admin.videos.editVideo(id)(formWithErrors)(VideoPlayer.allFormTuple)(VideoCategory.allFormTuple)),
        video => {
          Video.update(id, video)
          VideoHome.flashing("success" -> "Video %s has been updated".format(video.id))
        })
  }

  def deleteVideo(id: Int) = IsAuthenticated { _ =>
    _ =>
      Video.delete(id)
      VideoHome.flashing("success" -> "Video has been deleted")
  }

}