package models

import java.io.InputStream
import play.api.Play.current
import scala.io.Source
import play.api.Play
import java.io.IOException

sealed trait RugbyCategory
case object International extends RugbyCategory
case object AvivaPremiership extends RugbyCategory
case object RaboDirect extends RugbyCategory
case object Pro12 extends RugbyCategory
case object Top14 extends RugbyCategory
case object Super15 extends RugbyCategory
case object ExPlayers extends RugbyCategory

case class TableInfo(teamHead: String, playerHead: String, twitterHead: String)
case class Widget(id: Long, userHandle: String, listName: String)

trait DBData {
  
}

case class Player(name: String, twitterHandle: Option[String])
case class Team(name: String, twitterHandle: Option[String], link: String, players: Seq[Player], widget: Widget)
case class League(name: String, teams: Seq[Team])

trait CSVData {
 
  val internationalTeams = {
    val internationalTeamFiles = Seq(
        "Argentina" -> "argentina",
        "Australia" -> "australia",
        "England" -> "england",
        "France" -> "france",
        "Ireland" -> "ireland",
        "Italy" -> "italy",
        "Samoa" -> "manusamoa",
        "New Zealand" -> "nz",
        "South Africa" -> "sa",
        "Scotland" -> "scotland",
        "Tonga" -> "tonga",
        "Wales" -> "wales")
        
     val internationalWidgets = Map(
      "England" -> Widget(292025438644084737L, "Follow_Rugby", "england"),
      "Wales" -> Widget(292025297484783618L, "Follow_Rugby", "wales"),
      "Ireland" -> Widget(292025150117904384L, "Follow_Rugby", "ireland"),
      "Scotland" -> Widget(292024963949535232L, "Follow_Rugby", "scotland"),
      "France" -> Widget(292024793702735873L, "Follow_Rugby", "france"),
      "Italy" -> Widget(292024543126622210L, "Follow_Rugby", "italy"),
      "Argentina" -> Widget(292024225466814464L, "Follow_Rugby", "argentina"),
      "Australia" -> Widget(292024035225772032L, "Follow_Rugby", "australia"),
      "New Zealand" -> Widget(292023859073392640L, "Follow_Rugby", "new-zealand"),
      "South Africa" -> Widget(292023705746411521L, "Follow_Rugby", "south-africa"),
      "Samoa" -> Widget(292023576607985664L, "Follow_Rugby", "samoa"),
      "Tonga" -> Widget(292023437034127360L, "Follow_Rugby", "tonga"),
      "Ex Players" -> Widget(291672287927734273L, "Follow_Rugby", "ex-players")
    )
    
    mapToData("international", internationalTeamFiles, internationalWidgets)
   
  }
  
  val top14Teams = {
    val top14TeamFiles = Seq(
        "Agen Rugby" -> "agen",
        "Biarritz" -> "biarritz",
        "Bayonne" -> "bayonne",
        "Bordeaux" -> "bordeaux",
        "Clermont Auvergne" -> "clermont",
        "Castres" -> "castres",
        "Grenoble" -> "grenoble",
        "Montpellier" -> "montpellier",
        "Perpignan" -> "perpignan",
        "Racing Metro" -> "racingmetro",
        "Stade Francais" -> "stadefrancais",
        "Stade Montois" -> "stademontois",
        "Toulon" -> "toulon",
        "Toulouse" -> "toulouse")
        
    val top14Widgets = Map(
      "Toulon" -> Widget(291639009048928256L, "Follow_RugbyT14", "toulon"),
      "Stade Montois" -> Widget(291638398656053249L, "Follow_RugbyT14", "stademontois"),
      "Toulon" -> Widget(291639009048928256L, "Follow_RugbyT14", "toulon"),
      "Top14" -> Widget(291663641890144256L, "Follow_RugbyT14", "top-14-clubs"),
      "Perpignan" -> Widget(291663882198589440L, "Follow_RugbyT14", "perpignan"),
      "Bordeaux" -> Widget(291664102215000064L, "Follow_RugbyT14", "bordeaux-begles"),
      "Toulouse" -> Widget(291664264987557890L, "Follow_RugbyT14", "toulouse-17"),
      "Stade Francais" -> Widget(291664414610964480L, "Follow_RugbyT14", "stade-francais"),
      "Racing Metro" -> Widget(291664602603864064L, "Follow_RugbyT14", "racing-metro"),
      "Montpellier" -> Widget(291669182448279554L, "Follow_RugbyT14", "montpellier"),
      "Clermont Auvergne" -> Widget(291669407703371777L, "Follow_RugbyT14", "clermont"),
      "Grenoble" -> Widget(291669548514557954L, "Follow_RugbyT14", "grenoble"),
      "Castres" -> Widget(291669684292567044L, "Follow_RugbyT14", "castres"),
      "Biarritz" -> Widget(291669934264696832L, "Follow_RugbyT14", "biarritz"),
      "Bayonne" -> Widget(291639009048928256L, "Follow_RugbyT14", "bayonne"),
      "Agen Rugby" -> Widget(291670221247365120L, "Follow_RugbyT14", "agen-rugby")
  )
        
    mapToData("top14", top14TeamFiles, top14Widgets)
  }
  
  def generalTeamConsume(name: String, data: InputStream, widget: Widget) = {
    val consumedData = Source.fromInputStream(data, "ISO-8859-1").getLines.toSeq
    val teamTwitterHandle = consumedData.head.split(",") match {
      case deets if(deets.length < 2) => None
      case deets => Some(deets(1).split("@")(1).trim)
    }
    val link = name.replaceAll(" ", "")
    val players = for {
      line:String <- consumedData.tail
      details = line.split(",")  
      playerName = details(0)
      twitter = if(details.size > 1 && details(1).length > 2) Some(details(1).split("@")(1).trim) else None
    } yield Player(playerName, twitter)
    
    Team(name, teamTwitterHandle, link, players, widget)
  }
  
  private def mapToData(folderName: String, teamFiles: Seq[(String, String)], teamWidgets: Map[String, Widget]) = {
     val teams = teamFiles.map {
      case (name, fileName) =>
        val dataStream = Play.resourceAsStream("/" + folderName + "/" + fileName + ".csv") match {
          case Some(is) => is
          case _ => throw new IOException("file not found: " + fileName)
        }
        generalTeamConsume(name, dataStream, teamWidgets(name))
    }
    teams.map(t => t.link -> t).toMap
  }
}

object Data extends CSVData {
   val teams = Map(
    "International" -> internationalTeams,
    "Top14" -> top14Teams
  )
  val tableInfos = Map(
    "International" -> TableInfo("International Team", "Players", "Twitter Name"),
    "Top14" -> TableInfo("Top 14 Team", "Players", "Twitter Name")
  )
 
  
}

