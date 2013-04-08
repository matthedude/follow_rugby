package models

import play.api.Play.current
import com.typesafe.plugin._

object Email {
  def sendMail = {
  	val mail = use[MailerPlugin].email
    mail.setSubject("mailer")
    mail.addRecipient("me","matthedude@hotmail.com")
    mail.addFrom("PD")
    //sends html
    mail.sendHtml("<html>html</html>" )
    //sends text/text
    mail.send( "OYOYO" )
    //sends both text and html
    mail.send( "text", "<html>html</html>")
  }
}