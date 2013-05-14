package models

import play.api.Play.current
import com.typesafe.plugin._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object Email {
	val mail = use[MailerPlugin].email
  def sendEnterConfirmationMail() = {
  	val futureInt: Future[Unit] = scala.concurrent.Future {
	    mail.setSubject("Follow-Rugby.com British and Irish Lions 2013 squad competition Entry")
//    mail.addRecipient(entry.name + " <" + entry.email + ">")
//    mail.addFrom("Follow Rugby <ap@follow-rugby.com>")
    //sends html
//   mail.sendHtml
   
    
    }
	}
}