package models


import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

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

}




