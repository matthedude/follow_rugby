package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class CompEntry(name: String, email: String, password: String, phone: String, players: String)

object CompEntry {

  val simple = {
    get[String]("comp_entry.name") ~
      get[String]("comp_entry.email") ~
      get[String]("comp_entry.password") ~
      get[String]("comp_entry.phone") ~
      get[String]("comp_entry.players") map {
        case name ~ email ~ password ~ phone ~ players => CompEntry(name, email, password, phone, players)
      }
  }

  def all: Seq[CompEntry] = {
    DB.withConnection { implicit connection =>

      val compEntries = SQL(
        """
          select * from comp_entry
        """).as(CompEntry.simple *)

      compEntries
    }
  }

  def findByEmail(email: String): Option[CompEntry] = {
    DB.withConnection { implicit connection =>

      val entry = SQL(
        """
          select *
          from comp_entry 
          where email = {email}
        """).on('email -> email).as(CompEntry.simple singleOpt)

      entry
    }

  }

  def create(entry: CompEntry): Int = {
    DB.withConnection { implicit connection =>
      SQL("""
            insert into comp_entry (name, email, password, phone, players) values (
              {name}, {email}, {password}, {phone}, {players}
            )
            """).on(
        'name -> entry.name,
        'email -> entry.email,
        'password -> entry.password,
        'phone -> entry.phone,
        'players -> entry.players).executeUpdate()

    }
  }

  def update(entry: CompEntry): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update comp_entry
          set players = {players}
          where email = {email}
      		and password = {password}
        """).on(
          'players -> entry.players,
          'email -> entry.email,
          'password -> entry.password).executeUpdate()
    }

    if (loadEntry(entry.email, entry.password) == None) false else true
  }

  def loadEntry(email: String, password: String): Option[CompEntry] = {
    DB.withConnection { implicit connection =>

      val compEntry = SQL(
        """
          select * from comp_entry where email = {email} and password = {password}
        """).on('email -> email,
          'password -> password).as(CompEntry.simple singleOpt)

      compEntry
    }
  }

}