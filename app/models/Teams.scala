package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Team(id: Pk[Int] = NotAssigned, name: String, twitterName: Option[String], categoryId: Int, widgetId: Long, vidChannel: Option[String])

object Team {

  val blank = Team(name = "", twitterName = None, categoryId = 0, widgetId = 0L, vidChannel = None)
  val simple = {
    get[Pk[Int]]("team.id") ~
      get[String]("team.name") ~
      get[Option[String]]("team.twitter_name") ~
      get[Int]("team.category_id") ~
      get[Long]("team.widget_id") ~
      get[Option[String]]("team.vid_channel") map {
        case id ~ name ~ twitterName ~ categoryId ~ widgetId ~ vidChannel => Team(id, name, twitterName, categoryId, widgetId, vidChannel)
      }
  }

  val member = {
    get[Int]("team_member.member_id")
  }

  def all: Seq[Team] = {
    DB.withConnection { implicit connection =>

      val teams = SQL(
        """
          select * from team
        """).as(Team.simple *)

      teams
    }
  }

  def findByCategoryId(categoryId: Int): Seq[Team] = {
    DB.withConnection { implicit connection =>

      val teams = SQL(
        """
          select * from team
          where team.category_id = {categoryId}
        """).on('categoryId -> categoryId).as(Team.simple *)

      teams
    }
  }

  def findById(id: Int): Option[Team] = {
    DB.withConnection { implicit connection =>

      val team = SQL(
        """
          select *
          from team 
          where id = {id}
        """).on('id -> id).as(Team.simple singleOpt)

      team
    }

  }

  def findMembersById(id: Int): Seq[Int] = {
    DB.withConnection { implicit connection =>

      val members = SQL(
        """
          select member_id
          from team_member 
          where team_id = {id}
        """).on('id -> id).as(Team.member *)

      members
    }
  }

  def update(id: Int, team: Team) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update team
          set name = {name}, twitter_name = {twitterName}, category_id = {categoryId}, widget_id = {widgetId}, vid_channel = {vidChannel}
          where id = {id}
        """).on(
          'id -> id,
          'name -> team.name,
          'twitterName -> team.twitterName,
          'categoryId -> team.categoryId,
          'widgetId -> team.widgetId,
          'vidChannel -> team.vidChannel).executeUpdate()
    }
  }

  def updateTeamMembers(teamId: Int, newMembers: Set[Int]) = {
    val oldMembers = findMembersById(teamId).toSet
    val membersToDelete = oldMembers &~ newMembers
    val membersToInsert = newMembers &~ oldMembers

    membersToInsert foreach (insertTeamMember(teamId, _))
    membersToDelete foreach (deleteTeamMember(teamId, _))

    def insertTeamMember(teamId: Int, memberId: Int) = {
      DB.withConnection { implicit connection =>
        SQL(
          """
            insert into team_member (team_id, member_id) values (
              {teamId}, {memberId}
            )
          """).on(
            'teamId -> teamId,
            'memberId -> memberId).executeUpdate()
      }
    }

    def deleteTeamMember(teamId: Int, memberId: Int) = {
      DB.withConnection { implicit connection =>
        SQL("""
        		delete from team_member 
        		where team_id = {teamId}
        		and member_id = {memberId}
        		""").on(
          'teamId -> teamId,
          'memberId -> memberId).executeUpdate()
      }
    }
  }

  def delete(id: Int) = {
    DB.withConnection { implicit connection =>
      SQL("delete from team where id = {id}").on('id -> id).executeUpdate()
    }
  }

  def create(team: Team): Int = {
    DB.withConnection { implicit connection =>
      SQL("""
            insert into team (name, twitter_name, category_id, widget_id) values (
              {name}, {twitterName}, {categoryId}, {widgetId}
            )
            """).on(
        'name -> team.name,
        'twitterName -> team.twitterName,
        'categoryId -> team.categoryId,
        'widgetId -> team.widgetId,
        'vidChannel -> team.vidChannel).executeUpdate()

      val newTeam = SQL("""
            select *
        		from team
        		where name = {name}
        		and category_id = {categoryId}
        		and widget_id = {widgetId}
            """).on(
        'name -> team.name,
        'categoryId -> team.categoryId,
        'widgetId -> team.widgetId).as(Team.simple single)

      newTeam.id.get
    }
  }

  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1, filter: String = "%"): Page[Team] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val teams = SQL(
        """
          select * from team
          where team.name like {filter}
          order by {orderBy} 
          limit {pageSize} offset {offset}
        """).on(
          'pageSize -> pageSize,
          'offset -> offest,
          'filter -> filter,
          'orderBy -> orderBy).as(Team.simple *)

      val totalRows = SQL(
        """
          select count(*) from team
          where team.name like {filter}
        """).on(
          'filter -> filter).as(scalar[Long].single)

      Page(teams, page, offest, totalRows)

    }

  }

}