package models

import play.api.Play.current
import play.api.db._

import anorm._
import anorm.SqlParser._
import play.api.Play

case class Widget(id: Long, twitterAccount: String, url: String)

object Widget {

	val simple = {
		get[Long]("widget.id") ~
			get[String]("widget.twitter_account") ~
			get[String]("widget.url") map {
				case id ~ name ~ url => Widget(id, name, url)
			}
	}

	def all: Seq[Widget] = {
		DB.withConnection { implicit connection =>

			val widgets = SQL(
				"""
          select * from widget
        """).as(Widget.simple *)

			widgets
		}
	}

	def findById(id: Long): Option[Widget] = {
		DB.withConnection { implicit connection =>

			val widget = SQL(
				"""
          select *
          from widget 
          where id = {id}
        """).on('id -> id).as(Widget.simple singleOpt)

			widget
		}

	}

	def create(widget: Widget) = {
		DB.withConnection { implicit connection =>
			SQL("""
            insert into widget (id, twitter_account, url) values (
              {id}, {twitterAccount}, {url}
            )
            """).on(
				'id -> widget.id,
				'twitterAccount -> widget.twitterAccount,
				'url -> widget.url).executeUpdate()
		}
	}

	def update(id: Long, widget: Widget) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
          update widget
          set twitter_account = {twitterAccount}, url = {url}
          where id = {id}
        """).on(
					'twitterAccount -> widget.twitterAccount,
					'url -> widget.url,
					'id -> widget.id).executeUpdate()
		}
	}

	def delete(id: Long) = {
		DB.withConnection { implicit connection =>
			SQL("delete from widget where id = {id}").on('id -> id).executeUpdate()
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
	def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1, filter: String = "%"): Page[Widget] = {

		val offest = pageSize * page

		DB.withConnection { implicit connection =>

			val widgets = SQL(
				"""
          select * from widget
          where widget.url like {filter}
          order by {orderBy} 
          limit {pageSize} offset {offset}
        """).on(
					'pageSize -> pageSize,
					'offset -> offest,
					'filter -> filter,
					'orderBy -> orderBy).as(Widget.simple *)

			val totalRows = SQL(
				"""
          select count(*) from widget
          where widget.url like {filter}
        """).on(
					'filter -> filter).as(scalar[Long].single)

			Page(widgets, page, offest, totalRows)

		}

	}

}