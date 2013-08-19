package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class VideoPlayer(id: Pk[Int] = NotAssigned, name: String)

object VideoPlayer {
   
  val simple = {
    get[Pk[Int]]("video_player.id") ~
      get[String]("video_player.name") map {
        case id ~ name => VideoPlayer(id, name)
      }
  }

  def all: Seq[VideoPlayer] = {
    DB.withConnection { implicit connection =>

      val videoPlayers = SQL(
        """
          select * from video_player
        """).as(VideoPlayer.simple *)

      videoPlayers
    }
  }
  
  def findById(id: Int): Option[VideoPlayer] = {
    DB.withConnection { implicit connection =>

      val team = SQL(
        """
          select *
          from video_player 
          where id = {id}
        """).on('id -> id).as(VideoPlayer.simple singleOpt)

      team
    }

  }
}

case class VideoPlayerChannel(player: VideoPlayer, channel: String)