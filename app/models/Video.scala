package models

import play.api.Play.current

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play
import java.util.Date
import play.api.templates.Html

import java.util.HashMap;
import org.json.JSONArray;
import org.apache.commons.logging.LogFactory;
import com.embedly.api.Api;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
case class VideoHtml(video: Video, videoPlayer: VideoPlayer) {
  val (fullVideo:Html, thumbnail:String) = {
    videoPlayer.name.toLowerCase match {
      case "youtube" => {
        val l = """<iframe width="560" height="315" src="http://www.youtube.com/embed/""" + video.link + """" frameborder="0" allowfullscreen></iframe>"""
        val t = "http://img.youtube.com/vi/"+video.link+"/2.jpg"
        (Html(l), t) 
      }
      case "dailymotion" => {
        val l = """<iframe frameborder="0" width="480" height="270" src="http://www.dailymotion.com/embed/video/""" + video.link + """"></iframe>"""
        val t = "http://www.dailymotion.com/thumbnail/video/"+video.link
        (Html(l), t) 
      }
      case "other" => {
        val l = "<iframe src=\"https://media.embed.ly/1/frame?url=http%3A%2F%2Fwww.viddy.com%2Fvideo%2Fa46d893a-e82f-4998-8a15-424dbd3f40c5&width=748&secure=true&key=0202f0ddb5a3458aabf520e5ab790ab9&height=421\" width=\"748\" height=\"421\" border=\"0\" scrolling=\"no\" frameborder=\"0\"></iframe>"
        val t = "https://i.embed.ly/1/image?url=http%3A%2F%2Fcdn.viddy.com%2Fimages%2Fvideo%2Fa46d893a-e82f-4998-8a15-424dbd3f40c5.jpg&key=944875f32bd24eeab10c25f6636af91d"
        (Html(l), t) 
      }
    }
  }
  
}

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
  
  val withVideoCategoryPlayer = Video.simple ~ VideoCategory.simple ~ VideoPlayer.simple map {
    case video ~ videoCategory ~ videoPlayer => (video, videoCategory, videoPlayer)
  }
  
  val withVideoPlayer = Video.simple ~ VideoPlayer.simple map {
    case video ~ videoPlayer => (video, videoPlayer)
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
  
  def allWithVideoCategory: Seq[(Video, VideoCategory, VideoPlayer)] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video
          left join video_category 
          on video.video_category_id = video_category.id
          join video_player
          on video.video_player_id = video_player.id
          order by video.id desc
          limit 5
        """).as(Video.withVideoCategoryPlayer *)

      videos
    }
  }
  
  def latestForVideoCategoryWithPlayer(videoCategoryId: Int): Seq[(Video, VideoPlayer)] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video 
          left join video_player 
          on video.video_player_id = video_player.id
          where video.video_category_id = {videoCategoryId}
          order by video.id desc
          limit 5
        """).on('videoCategoryId -> videoCategoryId).as(Video.withVideoPlayer *)

      videos
    }
  }
  
  def randomForVideoCategoryWithPlayer(videoCategoryId: Int): Seq[(Video, VideoPlayer)] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video 
          left join video_player 
          on video.video_player_id = video_player.id
          where video.video_category_id = {videoCategoryId}
          order by RAND()
          limit 5
        """).on('videoCategoryId -> videoCategoryId).as(Video.withVideoPlayer *)

      videos
    }
  }
  
  def allWithVideoCategoryPlayerLatest: Seq[(Video, VideoCategory, VideoPlayer)] = {
    DB.withConnection { implicit connection =>

      val videos = SQL(
        """
          select * from video
          left join video_category 
          on video.video_category_id = video_category.id
          join video_player
          on video.video_player_id = video_player.id
          order by video.id desc
          limit 5
        """).as(Video.withVideoCategoryPlayer *)

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
  
  def update(id: Int, video: Video) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update video
          set video_category_id = {videoCategoryId}, video_player_id = {videoPlayerId}, link = {link}, description = {description}, title = {title}
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
//case class Video(id: Pk[Int] = NotAssigned, videoCategoryId: Int, videoPlayerId: Int, link: String, description: String, title: String, date: Date)
  def create(video: Video) = {
    val newLink = if(video.videoPlayerId == 3) {
      fetchEmbedlyLink(video.link)
    } else {
      video.link
    }
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
  
  private def fetchEmbedlyLink(link: String) = {
      val api = new Api("Mozilla/5.0 (compatible; followrugby/1.0; matthedude@hotmail.com)",
                    "3e6fbcd051d94fdcbecfd916d765aabb"); // <-- put key here
      val params = new HashMap[String, Object]()
      params.put("url", link);
      params.put("maxwidth", "560");

      val json = api.oembed(params);
      
      1 to json.
        
      
    ""
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
  def listForVideoCategory(page: Int = 0, pageSize: Int = 15, filter: String = "%", videoCategoryId: Int): Page[Video] = {
    
    val offest = pageSize * page
        
        DB.withConnection { implicit connection =>
        
        val videos = SQL(
            """
            select * from video
            where video.title like {filter}
            and video.video_category_id = {videoCategoryId} 
            order by video.id desc
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

