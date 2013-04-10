package models

import play.api.Play.current
import com.typesafe.plugin._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object Email {
	val mail = use[MailerPlugin].email
  def sendEnterConfirmationMail(players: Seq[String], entry: LionsCompTeam) = {
  	val futureInt: Future[Unit] = scala.concurrent.Future {
	    mail.setSubject("Follow-Rugby.com British and Irish Lions 2013 squad competition Entry")
    mail.addRecipient(entry.name + " <" + entry.email + ">")
    mail.addFrom("Follow Rugby <ap@follow-rugby.com>")
    //sends html
    mail.sendHtml("""<html><body>
  <p> Thank you for entering the www.Follow-Rugby.com British and Irish Lions 2013 squad competition.</p>

<p>You have chosen the following players: </p>
    
<ul>
""" + players.sorted.mkString("<li>", "</li><li>", "</li>") + """   
</ul>

<p>Please remember that you have until 10pm (UK BST) on April 25th to make changes to your selection. </p>

<p>Please forward this e-mail to your rugby obsessed friends and see who gets closest to Warren Gatland's chosen tour party!</p>

<p>Enjoy the site</p>

<p><a href="www.follow-rugby.com">www.Follow-Rugby.com</a></p>

<p><a href="https://twitter.com/intent/follow?original_referer=https://twitter.com/about/resources/buttons&region=follow_link&screen_name=FollowRugbySite&tw_p=followbutton&variant=2.0">@FollowRugbySite</a></p></body></html>""" )

	  }
   
    
  }
  
  def sendUpdateConfirmationMail(players: Seq[String], entry: LionsCompTeam)  = {
   val futureInt: Future[Unit] = scala.concurrent.Future {
    mail.setSubject("Follow-Rugby.com British and Irish Lions 2013 squad competition Squad Change")
    mail.addRecipient(entry.name + " <" + entry.email + ">")
    mail.addFrom("Follow Rugby <ap@follow-rugby.com>")
    val html = """<html><body>
  <p>Thank you updating your entry to the www.Follow-Rugby.com British and Irish Lions 2013 squad competition.</p>

<p>You have chosen the following players: </p>
    
<ul>
""" + players.sorted.mkString("<li>", "</li><li>", "</li>") + """   
</ul>

<p>Please remember that you have until 10pm (UK BST) on April 25th to make changes to your selection. </p>

<p>Please forward this e-mail to your rugby obsessed friends and see who gets closest to Warren Gatland's chosen tour party!</p>

<p>Enjoy the site</p>

<p><a href="www.follow-rugby.com">www.Follow-Rugby.com</a></p>

<p><a href="https://twitter.com/intent/follow?original_referer=https://twitter.com/about/resources/buttons&region=follow_link&screen_name=FollowRugbySite&tw_p=followbutton&variant=2.0">@FollowRugbySite</a></p></body></html>"""
//sends html

    mail.sendHtml(html)

   }


  }
}