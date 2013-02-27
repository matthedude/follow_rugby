package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Team(id: Pk[Int] = NotAssigned, name: String, categoryId: Int, widgetId: Long)

object Team {

  
  val simple = {
    get[Pk[Int]]("team.id") ~
    get[String]("team.name") ~
    get[Int]("team.category_id") ~ 
    get[Long]("team.widget_id") map {
      case id~name~categoryId~widgetId => Team(id, name, categoryId, widgetId)
    }
  }
  
  val member = {
  	get[Int]("team_member.member_id") 
  }
  
  def all:Seq[Team] = {
    DB.withConnection { implicit connection =>
      
      val teams = SQL(
        """
          select * from team
        """
      ).as(Team.simple *)
      
      teams
    }
  }
  
  def findByCategoryId(categoryId: Pk[Int]):Seq[Team] = {
    DB.withConnection { implicit connection =>
      
      val teams = SQL(
        """
          select * from team
          where team.category_id = {categoryId}
        """
      ).on('categoryId -> categoryId).as(Team.simple *)
      
      teams
    }
  }
  
  def findById(id: Int):Option[Team] = {
    DB.withConnection { implicit connection =>
      
      val team = SQL(
        """
          select *
          from team 
          where id = {id}
        """
      ).on('id -> id).as(Team.simple singleOpt)
      
      team
    }
  
  }
  
  def findMembersById(id: Int):Seq[Int] = {
  	 DB.withConnection { implicit connection =>
      
      val members = SQL(
        """
          select member_id
          from team_member 
          where team_id = {id}
        """
      ).on('id -> id).as(Team.member *)
      
      members
    }
  }
 
  def update(id: Int, team: Team) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update team
          set name = {name}, category_id = {categoryId}, widget_id = {widgetId}
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> team.name,
        'categoryId -> team.categoryId,
        'widgetId -> team.widgetId
      ).executeUpdate()
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
          """
        ).on(
          'teamId -> teamId,
          'memberId -> memberId
        ).executeUpdate()
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
          'memberId -> memberId
        ).executeUpdate()
      }
  	}
  }
  
  def delete(id: Int) = {
    DB.withConnection { implicit connection =>
      SQL("delete from team where id = {id}").on('id -> id).executeUpdate()
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
        """
      ).on(
        'pageSize -> pageSize, 
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Team.simple *)

      val totalRows = SQL(
        """
          select count(*) from team
          where team.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(teams, page, offest, totalRows)
      
    }
    
  }
  
}