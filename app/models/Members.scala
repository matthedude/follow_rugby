package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Member(id: Pk[Int] = NotAssigned, name: String, twitterName: String, teamId: Int)


object Member {

  
  val simple = {
    get[Pk[Int]]("member.id") ~
    get[String]("member.name") ~
    get[String]("member.twitter_name") ~ 
    get[Int]("member.team_id") map {
      case id~name~twitterName~teamId => Member(id, name, twitterName, teamId)
    }
  }
  
  def findByTeamId(teamId: Pk[Int]):Seq[Member] = {
    DB.withConnection { implicit connection =>
      
      val players = SQL(
        """
          select * from member
          where member.team_id = {teamId}
        """
      ).on('teamId -> teamId).as(Member.simple *)
      
      players
    }
  }
  
}