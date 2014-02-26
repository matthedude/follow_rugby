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
        case id ~ name ~ twitterName => Member(id, name, twitterName)
      }
  }

  val withTeam = Member.simple ~ str("name") map {
    case member ~ team => (member, team)
  }

  val withTeamCategory = Member.simple ~ Team.simple ~ Category.simple map {
    case member ~ team ~ category => (member, team, category)
  }

  def all: Seq[Member] = {
    DB.withConnection { implicit connection =>

      val members = SQL(
        """
          select * from member
        """).as(Member.simple *)

      members
    }
  }

  def latestMembersWithTeamCategory: Seq[(Member, Team, Category)] = {
    DB.withConnection { implicit connection =>

      val members = SQL(
        """
          select member.id, member.name, member.twitter_name, team.id, team.id, team.name, team.twitter_name, team.category_id, team.widget_id, team.vid_channel, team.video_player_id, team.hashtag, category.id, category.name, category.widget_id
          from member 
          left join team_member 
          on member.id = team_member.member_id
          join team on
          team_member.team_id = team.id
          join category
          on team.category_id = category.id
          order by member.id desc
          limit 10
        """).as(Member.withTeamCategory *)

      members.map { case (m, t, c) => (m, (m, t, c)) }.toMap.values.toList
    }
  }

  def findByTeamId(teamId: Int): Seq[Member] = {
    DB.withConnection { implicit connection =>

      val members = SQL(
        """
          select m.id, m.name, m.twitter_name 
          from member m join team_member tm 
          on m.id = tm.member_id 
          where tm.team_id = {teamId}
        """).on('teamId -> teamId).as(Member.simple *)

      members
    }
  }

  def findById(id: Int): Option[Member] = {
    DB.withConnection { implicit connection =>

      val member = SQL(
        """
          select *
          from member 
          where id = {id}
        """).on('id -> id).as(Member.simple singleOpt)

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
        'twitterName -> member.twitterName).executeUpdate()
    }
  }

  def update(id: Int, member: Member) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update member
          set name = {name}, twitter_name = {twitterName}
          where id = {id}
        """).on(
          'id -> id,
          'name -> member.name,
          'twitterName -> member.twitterName).executeUpdate()
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
        """).on(
          'pageSize -> pageSize,
          'offset -> offest,
          'filter -> filter,
          'orderBy -> orderBy).as(Member.simple *)

      val totalRows = SQL(
        """
          select count(*) from member
          where member.name like {filter}
        """).on(
          'filter -> filter).as(scalar[Long].single)

      Page(members, page, offest, totalRows)

    }

  }
  def search(filter: String = "%"):SearchResults  = {

    DB.withConnection { implicit connection =>

      val members = SQL(
        """
            select * from member
            where member.name like {filter}
            or member.twitter_name like {filter}
            """).on(
          'filter -> filter).as(Member.simple *)
      val teams = SQL(
         """
            select * from team
            where team.name like {filter}
            or team.twitter_name like {filter}
            """).on(
          'filter -> filter).as(Team.simple *)
          
      SearchResults(members, teams.filter(_.twitterName.isDefined))
    }

  }

}

case class SearchResults(members: Seq[Member] = Nil, teams: Seq[Team] = Nil)