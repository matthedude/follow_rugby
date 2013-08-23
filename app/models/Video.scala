package models

import play.api.Play.current
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play
import java.util.Date

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

      val player = SQL(
        """
          select *
          from video_player 
          where id = {id}
        """).on('id -> id).as(VideoPlayer.simple singleOpt)

      player
    }

  }
  
  def allFormTuple = {
    all.map(v => (v.id.get.toString, v.name))
  }
  
 
}

case class VideoPlayerChannel(player: VideoPlayer, channel: String)

case class Video(id: Pk[Int] = NotAssigned, videoCategoryId: Int, videoPlayerId: Int, link: String, description: String, title: String, date: Date)

object Video {
  val simple = {
    get[Pk[Int]]("video.id") ~
    get[Int]("video.video_category_id") ~
    get[Int]("video.video_player_id") ~
    get[String]("video.link") ~
    get[String]("video.description") ~
    get[String]("video.title") ~
    get[Date]("video.date")  map {
        case id ~ videoCategoryId ~ videoPlayerId ~ link ~ description ~ title ~ date  => Video(id, videoCategoryId, videoPlayerId, link, description, title, date)
      }
  }
  
  val withVideoCategory = Video.simple ~ VideoCategory.simple map {
    case video ~ videoCategory => (video, videoCategory)
  }

  def all: Seq[Video] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video
        """).as(Video.simple *)

      videos
    }
  }
  
  def allWithVideoCategory: Seq[(Video, VideoCategory)] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video
          left join video_category 
          on video.video_category_id = video_category.id
        """).as(Video.withVideoCategory *)

      videos
    }
  }
  
  def allForVideoCategory(videoCategoryId: Int): Seq[Video] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video 
          where video.video_category_id = {videoCategoryId}
        """).on('videoCategoryId -> videoCategoryId).as(Video.simple *)

      videos
    }
  }
  
  def allWithVideoCategoryLatest: Seq[(Video, VideoCategory)] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video
          left join video_category 
          on video.video_category_id = video_category.id
          order by video.id desc
          limit 10
        """).as(Video.withVideoCategory *)

      videos
    }
  }
  
  def findById(id: Int): Option[Video] = {
    DB.withConnection { implicit connection =>

      val video = SQL(
        """
          select *
          from video 
          where id = {id}
        """).on('id -> id).as(Video.simple singleOpt)

      video
    }

  }
  
  def delete(id: Int) = {
    DB.withConnection { implicit connection =>
      SQL("delete from video where id = {id}").on('id -> id).executeUpdate()
    }
  }
//case class Video(id: Pk[Int] = NotAssigned, videoCategoryId: Int, videoPlayerId: Int, link: String, description: String, title: String, date: Date)
  def create(video: Video) = {
    DB.withConnection { implicit connection =>
      SQL("""
            insert into video (video_category_id, video_player_id, link, description, title) values (
              {videoCategoryId}, {videoPlayerId}, {link}, {description}, {title}
            )
            """).on(
        'videoCategoryId -> video.videoCategoryId,
        'videoPlayerId -> video.videoPlayerId,
        'link -> video.link,
        'description -> video.description,
        'title -> video.title).executeUpdate()

      
    }
  }
  
  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1, filter: String = "%"): Page[Video] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video
          where video.title like {filter}
          order by {orderBy} 
          limit {pageSize} offset {offset}
        """).on(
          'pageSize -> pageSize,
          'offset -> offest,
          'filter -> filter,
          'orderBy -> orderBy).as(Video.simple *)

      val totalRows = SQL(
        """
          select count(*) from video
          where video.title like {filter}
        """).on(
          'filter -> filter).as(scalar[Long].single)

      Page(videos, page, offest, totalRows)

    }

  }
  def listForVideoCategory(page: Int = 0, pageSize: Int = 20, filter: String = "%", videoCategoryId: Int): Page[Video] = {
    
    val offest = pageSize * page
        
        DB.withConnection { implicit connection =>
        
        val videos = SQL(
            """
            select * from video
            where video.title like {filter}
            and video.video_category_id = {videoCategoryId} 
            limit {pageSize} offset {offset}
            """).on(
                'pageSize -> pageSize,
                'offset -> offest,
                'filter -> filter,
                'videoCategoryId -> videoCategoryId).as(Video.simple *)
                
                val totalRows = SQL(
                    """
                    select count(*) from video
                    where video.title like {filter}
                    and video.video_category_id = {videoCategoryId} 
                    """).on(
                        'filter -> filter,
                'videoCategoryId -> videoCategoryId).as(scalar[Long].single)
                        
                        Page(videos, page, offest, totalRows)
                        
    }
    
  }
  
  
  
  def update(id: Int, video: Video) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update video
          set video_category_id = {name}, video_player_id = {videoPlayerId}, link = {link}, description = {description}, title = {title}
          where id = {id}
        """).on(
        'id -> id,
        'videoCategoryId -> video.videoCategoryId,
        'videoPlayerId -> video.videoPlayerId,
        'link -> video.link,
        'description -> video.description,
        'title -> video.title).executeUpdate()
    }
  }
}

case class VideoCategory(id: Pk[Int] = NotAssigned, name: String)

object VideoCategory {
  val simple = {
    get[Pk[Int]]("video_category.id") ~
      get[String]("video_category.name") map {
        case id ~ name => VideoCategory(id, name)
      }
  }

  def all: Seq[VideoCategory] = {
    DB.withConnection { implicit connection =>

      val videoCategories = SQL(
        """
          select * from video_category
        """).as(VideoCategory.simple *)

      videoCategories
    }
  }
  def allFormTuple = {
    all.map(v => (v.id.get.toString, v.name))
  }
  
  
  def findById(id: Int): Option[VideoCategory] = {
    DB.withConnection { implicit connection =>

      val category = SQL(
        """
          select *
          from video_category 
          where id = {id}
        """).on('id -> id).as(VideoCategory.simple singleOpt)

      category
    }

  }
  
  
  
}