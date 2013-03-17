package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Member(id: Pk[Int] = NotAssigned, name: String, twitterName: String)


object Member {

  
  val simple = {
    get[Pk[Int]]("member.id") ~
    get[String]("member.name") ~
    get[String]("member.twitter_name") map {
      case id~name~twitterName => Member(id, name, twitterName)
    }
  }
  
  val withTeam = Member.simple ~ str("name") map {
    case member~team=> (member, team)
  }
  
  def all:Seq[Member] = {
    DB.withConnection { implicit connection =>
      
      val members = SQL(
        """
          select * from member
        """
      ).as(Member.simple *)
      
      members
    }
  }
  
  def findByTeamId(teamId: Pk[Int]):Seq[Member] = {
    DB.withConnection { implicit connection =>
      
      val members = SQL(
        """
          select m.id, m.name, m.twitter_name 
          from member m join team_member tm 
          on m.id = tm.member_id 
          where tm.team_id = {teamId}
        """
      ).on('teamId -> teamId).as(Member.simple *)
      
      members
    }
  }
  
  def findById(id: Int):Option[Member] = {
    DB.withConnection { implicit connection =>
      
      val member = SQL(
        """
          select *
          from member 
          where id = {id}
        """
      ).on('id -> id).as(Member.simple singleOpt)
      
      member
    }
  
  }
  
  def create(member: Member) = {
    DB.withConnection { implicit connection =>
        SQL("""
            insert into member (name, twitter_name) values (
              {name}, {twitterName}
            )
            """).on(
          'name -> member.name,
          'twitterName -> member.twitterName
        ).executeUpdate()
    }
  }
 
  def update(id: Int, member: Member) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update member
          set name = {name}, twitter_name = {twitterName}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> member.name,
        'twitterName -> member.twitterName
      ).executeUpdate()
    }
  }
  
  def delete(id: Int) = {
    DB.withConnection { implicit connection =>
      SQL("delete from member where id = {id}").on('id -> id).executeUpdate()
    }
  }
  
  /**
   * Return a page of (Computer,Company).
   *
   * @param page Page to display
   * @param pageSize Number of computers per page
   * @param orderBy Computer property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1, filter: String = "%"): Page[Member] = {
    
    val offest = pageSize * page
    
    DB.withConnection { implicit connection =>
      
 
      
      
      val members = SQL(
        """
          select * from member
          where member.name like {filter}
          order by {orderBy} 
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize, 
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Member.simple *)

      val totalRows = SQL(
        """
          select count(*) from member
          where member.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(members, page, offest, totalRows)
      
    }
    
  }
  
}