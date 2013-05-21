package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Category(id: Pk[Int] = NotAssigned, name: String, widgetId: Option[Long])

object Category {

  def all: Seq[Category] = {
    DB.withConnection { implicit connection =>

      val categories = SQL(
        """
          select * from category
        """).as(Category.simple *)

      categories
    }
  }

  val simple = {
    get[Pk[Int]]("category.id") ~
      get[String]("category.name") ~
      get[Option[Long]]("category.widget_id") map {
        case id ~ name ~ widgetId => Category(id, name, widgetId)
      }
  }

  def findById(id: Int): Option[Category] = {
    DB.withConnection { implicit connection =>

      val category = SQL(
        """
          select *
          from category 
          where id = {id}
        """).on('id -> id).as(Category.simple singleOpt)

      category
    }

  }

}