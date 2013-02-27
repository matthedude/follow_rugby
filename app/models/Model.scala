package models


import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class TeamMembers(team: Team, members: List[Int])

case class MatchCentreGame(team1: Team, team2: Team, game: Game, team1Widget: Widget, team2Widget: Widget)

object Data {
  
  val categories:Seq[Category] = Category.all
  
  val idToCategories:Map[Int, Category] = {categories map (c => c.id.get -> c)}.toMap
  
  val categoriesToTeams:Map[Category, Seq[Team]] =
    {categories map (category => (category, Team.findByCategoryId(category.id)))}.toMap
  
  val idToTeams:Map[Int, Team] = 
    {Team.all map (t => t.id.get -> t)}.toMap
    
  val teamsToMembers:Map[Team, Seq[Member]] = 
    {(categoriesToTeams.values flatten) map (team => (team, Member.findByTeamId(team.id)))}.toMap
    
  val idToWidgets:Map[Long, Widget] = 
    {Widget.all map (w => w.id.get -> w)}.toMap
    
  val matches:Seq[MatchCentreGame] = 
    Game.all map { game => 
      val t1 = idToTeams(game.team1Id)
      val t2 = idToTeams(game.team2Id)
      MatchCentreGame(t1, t2, game, idToWidgets(t1.widgetId), idToWidgets(t2.widgetId))
    }
    

}




