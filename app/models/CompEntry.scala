package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class CompEntry(name: String, email: String, phone: String, players: String)

object CompEntry {
	
  val simple = {
    get[String]("comp_entry.name") ~
    get[String]("comp_entry.email") ~
    get[String]("comp_entry.phone") ~
    get[String]("comp_entry.players") map {
      case name~email~phone~players => CompEntry(name, email, phone, players)
    }
  }
  
  def all:Seq[CompEntry] = {
    DB.withConnection { implicit connection =>
      
      val compEntries = SQL(
        """
          select * from comp_entry
        """
      ).as(CompEntry.simple *)
      
      compEntries
    }
  }
  
  def findByEmail(email: String):Option[CompEntry] = {
    DB.withConnection { implicit connection =>
      
      val entry = SQL(
        """
          select *
          from comp_entry 
          where email = {email}
        """
      ).on('email -> email).as(CompEntry.simple singleOpt)
      
      entry
    }
  
  }
  
  def create(entry: CompEntry):Int = {
    DB.withConnection { implicit connection =>
        SQL("""
            insert into comp_entry (name, email, phone, players) values (
              {name}, {email}, {phone}, {players}
            )
            """).on(
          'name -> entry.name,
          'email -> entry.email,
          'phone -> entry.phone,
          'players -> entry.players
        ).executeUpdate()
        
    }
  }
  
  
}