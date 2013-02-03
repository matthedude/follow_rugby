package models

import java.io.InputStream
import play.api.Play.current
import scala.io.Source
import play.api.Play
import java.io.IOException

case class CategoryViewInfo(teamHead: String, playerHead: String, twitterHead: String, widget: Option[WidgetDB])
case class WidgetDB(id: Long, twitterAccount: String, url: String)


case class PlayerDB(name: String, twitterHandle: Option[String])
case class TeamDB(name: String, twitterHandle: Option[String], link: String, players: Seq[PlayerDB], widget: WidgetDB)
case class League(name: String, teams: Seq[TeamDB])


case class GroupDB(name: String, widgetId: Long, category:String)
case class MemberDB(name: String, twitterAccount: String, group: String)
case class CategoryDB(name: String, widgetId: Option[Long])

object ConsumeCSV extends CSVData {
   val teams = Map(
    "International" -> internationalTeams,
    "Super15" -> super15Teams,
    "Top14" -> top14Teams,
    "AvivaPremiership" -> avivaPremTeams,
    "Pro12" -> pro12Teams
  )
  val tableInfos = Map(
    "International" -> CategoryViewInfo("International Team", "Player", "Twitter Name", None),
    "Super 15" -> CategoryViewInfo("Super 15 Team", "Player", "Twitter Name", Some(WidgetDB(293103940323639297L, "Follow_RugbyS15", "super-15"))),
    "Top 14" -> CategoryViewInfo("Top 14 Team", "Player", "Twitter Name", Some(WidgetDB(291663641890144256L, "Follow_RugbyT14", "top-14-clubs"))),
    "Aviva Premiership" -> CategoryViewInfo("Aviva Premiership Team", "Player", "Twitter Name", Some(WidgetDB(293090598284566529L, "Follow_RugbyPR", "premiership-rugby-teams"))),
    "Pro 12" -> CategoryViewInfo("Pro 12 Team", "Player", "Twitter Name", Some(WidgetDB(292718406866706433L, "Follow_RugbyP12", "pro-12"))) 
  )
 
     
    import anorm._
     import play.api.db.DB
     
     def createMember(member: MemberDB) {
      DB.withConnection {
        implicit connection =>
          SQL("""insert into member(name, twitter_name, team_id) 
              values ({name}, {twitterAccount}, (select id from team where name = {group} ));""").on(
            'name -> member.name,
            'twitterAccount-> member.twitterAccount,
            'group -> member.group
          ).executeUpdate()
      }
    }
     
     def createWidget(widget: WidgetDB) {
      DB.withConnection {
        implicit connection =>
          SQL("insert into widget(id, twitter_account, url) values ({id}, {twitterAccount}, {url});").on(
            'id -> widget.id,
            'twitterAccount-> widget.twitterAccount,
            'url -> widget.url
          ).executeUpdate()
      }
    }
   
   def createTeam(team: GroupDB) {
      DB.withConnection {
        implicit connection =>
          SQL("""insert into team(name, widget_id, category_id) 
              values ({name}, {widgetId}, (select id from category where name = {category}))
              ;""").on(
            'name -> team.name,
            'widgetId-> team.widgetId,
            'category -> team.category
          ).executeUpdate()
      }
    }
   
   def createCategory(category: CategoryDB) {
      DB.withConnection {
        implicit connection =>
          SQL("insert into category(name, widget_id) values ({name}, {widget_id});").on(
            'name -> category.name,
            'widget_id-> category.widgetId
          ).executeUpdate()
      }
    }
   
    top14Teams.map(_._2.widget).foreach(createWidget)
    internationalTeams.map(_._2.widget).foreach(createWidget)
    super15Teams.map(_._2.widget).foreach(createWidget)
    pro12Teams.map(_._2.widget).foreach(createWidget)
    avivaPremTeams.map(_._2.widget).foreach(createWidget)
   
   tableInfos.map(_._2.widget).foreach(w => if(w.isDefined)createWidget(w.get))
   
   val cats = List(
       CategoryDB("International", None),
       CategoryDB("Super 15", Some(293103940323639297L)),
       CategoryDB("Top 14", Some(291663641890144256L)),
       CategoryDB("Aviva Premiership", Some(293090598284566529L)),
       CategoryDB("Pro 12", Some(292718406866706433L)))
   
   cats foreach createCategory

   super15Teams.map {
     case (_, team) => GroupDB(team.name, team.widget.id, "Super 15")
   }.foreach(createTeam)
   
   top14Teams.map {
     case (_, team) => GroupDB(team.name, team.widget.id, "Top 14")
   }.foreach(createTeam)
   
   pro12Teams.map {
     case (_, team) => GroupDB(team.name, team.widget.id, "Pro 12")
   }.foreach(createTeam)
   
   internationalTeams.map {
     case (_, team) => GroupDB(team.name, team.widget.id, "International")
   }.foreach(createTeam)
   
   avivaPremTeams.map {
     case (_, team) => GroupDB(team.name, team.widget.id, "Aviva Premiership")
   }.foreach(createTeam)
   
   super15Teams.flatMap {
     case (_, team) => team.players.filter(_.twitterHandle.isDefined).map(p => MemberDB(p.name, p.twitterHandle.get, team.name))
   }.foreach(createMember)
   
    top14Teams.flatMap {
     case (_, team) => team.players.filter(_.twitterHandle.isDefined).map(p => MemberDB(p.name, p.twitterHandle.get, team.name))
   }.foreach(createMember)
   
    pro12Teams.flatMap {
     case (_, team) => team.players.filter(_.twitterHandle.isDefined).map(p => MemberDB(p.name, p.twitterHandle.get, team.name))
   }.foreach(createMember)
   
   
   internationalTeams.flatMap {
     case (_, team) => team.players.filter(_.twitterHandle.isDefined).map(p => MemberDB(p.name, p.twitterHandle.get, team.name))
   }.foreach(createMember)
   
   avivaPremTeams.flatMap {
     case (_, team) => team.players.filter(_.twitterHandle.isDefined).map(p => MemberDB(p.name, p.twitterHandle.get, team.name))
   }.foreach(createMember)
}

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
      "England" -> WidgetDB(292025438644084737L, "Follow_Rugby", "england"),
      "Wales" -> WidgetDB(292025297484783618L, "Follow_Rugby", "wales"),
      "Ireland" -> WidgetDB(292025150117904384L, "Follow_Rugby", "ireland"),
      "Scotland" -> WidgetDB(292024963949535232L, "Follow_Rugby", "scotland"),
      "France" -> WidgetDB(292024793702735873L, "Follow_Rugby", "france"),
      "Italy" -> WidgetDB(292024543126622210L, "Follow_Rugby", "italy"),
      "Argentina" -> WidgetDB(292024225466814464L, "Follow_Rugby", "argentina"),
      "Australia" -> WidgetDB(292024035225772032L, "Follow_Rugby", "australia"),
      "New Zealand" -> WidgetDB(292023859073392640L, "Follow_Rugby", "new-zealand"),
      "South Africa" -> WidgetDB(292023705746411521L, "Follow_Rugby", "south-africa"),
      "Samoa" -> WidgetDB(292023576607985664L, "Follow_Rugby", "samoa"),
      "Tonga" -> WidgetDB(292023437034127360L, "Follow_Rugby", "tonga"),
      "Ex Players" -> WidgetDB(291672287927734273L, "Follow_Rugby", "ex-players")
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
      "Toulon" -> WidgetDB(291639009048928256L, "Follow_RugbyT14", "toulon"),
      "Stade Montois" -> WidgetDB(291638398656053249L, "Follow_RugbyT14", "stademontois"),
      "Top14" -> WidgetDB(291663641890144256L, "Follow_RugbyT14", "top-14-clubs"),
      "Perpignan" -> WidgetDB(291663882198589440L, "Follow_RugbyT14", "perpignan"),
      "Bordeaux" -> WidgetDB(291664102215000064L, "Follow_RugbyT14", "bordeaux-begles"),
      "Toulouse" -> WidgetDB(291664264987557890L, "Follow_RugbyT14", "toulouse-17"),
      "Stade Francais" -> WidgetDB(291664414610964480L, "Follow_RugbyT14", "stade-francais"),
      "Racing Metro" -> WidgetDB(291664602603864064L, "Follow_RugbyT14", "racing-metro"),
      "Montpellier" -> WidgetDB(291669182448279554L, "Follow_RugbyT14", "montpellier"),
      "Clermont Auvergne" -> WidgetDB(291669407703371777L, "Follow_RugbyT14", "clermont"),
      "Grenoble" -> WidgetDB(291669548514557954L, "Follow_RugbyT14", "grenoble"),
      "Castres" -> WidgetDB(291669684292567044L, "Follow_RugbyT14", "castres"),
      "Biarritz" -> WidgetDB(291669934264696832L, "Follow_RugbyT14", "biarritz"),
      "Bayonne" -> WidgetDB(291670079786061824L, "Follow_RugbyT14", "bayonne"),
      "Agen Rugby" -> WidgetDB(291670221247365120L, "Follow_RugbyT14", "agen-rugby")
  )
        
    mapToData("top14", top14TeamFiles, top14Widgets)
  }
  
  val super15Teams = {
    val super15TeamFiles = Seq(
      "Brumbies" -> "brumbies",   
      "The Highlanders" -> "highlanders",
      "Melbourne Rebels" -> "melbourne",
      "Queensland Reds" -> "queensland",
      "The Stormers" -> "stormers",
      "The Crusaders" -> "crusaders",
      "The Hurricanes" -> "hurricanes",
      "New South Wales Waratahs" -> "nsw",
      "The Sharks" -> "sharks",
      "The Western Force" -> "westernforce"
    )
    
    val super15Widgets = Map(
      "Super 15 Teams" -> WidgetDB(292611168047075328L, "Follow_RugbyS15", "super-15-teams"),
      "The Sharks" -> WidgetDB(293099620165361664L, "Follow_RugbyS15", "the-sharks"),
      "The Stormers" -> WidgetDB(293099968737181696L, "Follow_RugbyS15", "the-stormers"),
      "The Western Force" -> WidgetDB(293100250531512320L, "Follow_RugbyS15", "the-western-force"),
      "Brumbies" -> WidgetDB(293100543629459456L, "Follow_RugbyS15", "brumbies"),
      "Queensland Reds" -> WidgetDB(293100806541021184L, "Follow_RugbyS15", "queensland-reds"),
      "New South Wales Waratahs" -> WidgetDB(293101073932095489L, "Follow_RugbyS15", "new-south-wales-waratahs"),
      "Melbourne Rebels" -> WidgetDB(293101360897990656L, "Follow_RugbyS15", "melbourne-rebels"),
      "The Hurricanes" -> WidgetDB(293101570143424513L, "Follow_RugbyS15", "hurricanes"),
      "The Highlanders" -> WidgetDB(293101806454718464L, "Follow_RugbyS15", "the-highlanders"),
      "The Crusaders" -> WidgetDB(293102136068276225L, "Follow_RugbyS15", "the-crusaders"),
      "New Zealand Conference" -> WidgetDB(293102351378677760L, "Follow_RugbyS15", "new-zealand-conference"),
      "Australian Conference" -> WidgetDB(293102563396554753L, "Follow_RugbyS15", "australian-conference"),
      "South African Conference" -> WidgetDB(293102786709700608L, "Follow_RugbyS15", "south-african-conference"),
      "The Chiefs" -> WidgetDB(293103002624073729L, "Follow_RugbyS15", "the-chiefs"),
      "Cheetahs" -> WidgetDB(293103231943450624L, "Follow_RugbyS15", "cheetahs"),
      "The Blues" -> WidgetDB(293103709158785024L, "Follow_RugbyS15", "the-blues"),
      "Super 15" -> WidgetDB(293103940323639297L, "Follow_RugbyS15", "super-15"),
      "Blue Bulls" -> WidgetDB(292614787366522881L, "Follow_RugbyS15", "blue-bulls")
    )
    
    mapToData("super15", super15TeamFiles, super15Widgets)
  }
  
  val pro12Teams = {
    val pro12TeamFiles = Seq(
        "Treviso" -> "benetton",
        "Cardiff Blues" -> "cardiff",
        "Connacht" -> "connacht",
        "Newport Gwent Dragons" -> "dragons",
        "Glasgow" -> "glasgow",
        "Leinster" -> "leinster",
        "Munster" -> "munster",
        "Ospreys" -> "ospreys",
        "Llanelli Scarlets" -> "scarlets",
        "Ulster" -> "ulster")
       

    val pro12Widgets = Map(
      "Pro 12 Clubs" -> WidgetDB(293094801786667008L, "Follow_RugbyP12", "pro-12-clubs"),
      "Edinburgh" -> WidgetDB(293095122143428608L, "Follow_RugbyP12", "edinburgh"),
      "Zebre" -> WidgetDB(293095378260205570L, "Follow_RugbyP12", "zebre"),
      "Ulster" -> WidgetDB(293095599845281792L, "Follow_RugbyP12", "ulster"),
      "Llanelli Scarlets" -> WidgetDB(293095843190407168L, "Follow_RugbyP12", "llanelli-scarlets"),
      "Ospreys" -> WidgetDB(293096105888063488L, "Follow_RugbyP12", "ospreys"),
      "Munster" -> WidgetDB(293096317557809153L, "Follow_RugbyP12", "munster"),
      "Leinster" -> WidgetDB(293096543744032769L, "Follow_RugbyP12", "leinster"),
      "Glasgow" -> WidgetDB(293096845918486529L, "Follow_RugbyP12", "glasgow"),
      "Scottish Teams" -> WidgetDB(293097071429427202L, "Follow_RugbyP12", "scottish-teams"),
      "Newport Gwent Dragons" -> WidgetDB(293097307438727169L, "Follow_RugbyP12", "newport-gwent-dragons"),
      "Connacht" -> WidgetDB(293097613274783744L, "Follow_RugbyP12", "connacht"),
      "Irish Teams" -> WidgetDB(293097926408945664L, "Follow_RugbyP12", "irish-teams"),
      "Cardiff Blues" -> WidgetDB(293098198581510144L, "Follow_RugbyP12", "cardiff-blues"),
      "Wales Teams" -> WidgetDB(293098524453765122L, "Follow_RugbyP12", "wales-teams"),
      "Italian Teams" -> WidgetDB(293098791144394752L, "Follow_RugbyP12", "italian-teams"),
      "Treviso" -> WidgetDB(293099051891695619L, "Follow_RugbyP12", "treviso"),
      "Pro 12" -> WidgetDB(292718406866706433L, "Follow_RugbyP12", "pro-12")
  )
        
    mapToData("pro12", pro12TeamFiles, pro12Widgets)
  }
  
   val avivaPremTeams = {
    val premTeamFiles = Seq(
        "Bath" -> "bath",
        "Exeter Chiefs" -> "exeter",
        "Gloucester" -> "gloucester",
        "Harlequins" -> "harlequins",
        "Leicester Tigers" -> "leicester",
        "London Irish" -> "londonIrish",
        "London Welsh" -> "londonWelsh",
        "Northampton Saints" -> "northampton",
        "Sale Sharks" -> "sale",
        "Saracens" -> "saracens",
        "London Wasps" -> "wasps",
        "Worcester Warriors" -> "worcester")
 

    val premWidgets = Map(
      "Premiership Rugby Teams" -> WidgetDB(293090598284566529L, "Follow_RugbyPR", "premiership-rugby-teams"),
"Worcester Warriors" -> WidgetDB(293085374954213377L, "Follow_RugbyPR", "worcester-warriors"),
"Saracens" -> WidgetDB(293091005249503232L, "Follow_RugbyPR", "saracens"),
"Sale Sharks" -> WidgetDB(293091259747287040L, "Follow_RugbyPR", "sale-sharks"),
"Northampton Saints" -> WidgetDB(293091621183029251L, "Follow_RugbyPR", "northampton-saints"),
"Harlequins" -> WidgetDB(293091906274082818L, "Follow_RugbyPR", "harlequins"),
"London Welsh" -> WidgetDB(293092119067893761L, "Follow_RugbyPR", "london-welsh"),
"London Wasps" -> WidgetDB(293092398798606336L, "Follow_RugbyPR", "london-wasps"),
"London Irish" -> WidgetDB(293092675853369346L, "Follow_RugbyPR", "london-irish"),
"Leicester Tigers" -> WidgetDB(293093648860577793L, "Follow_RugbyPR", "leicester-tigers"),
"Gloucester" -> WidgetDB(293093137839165440L, "Follow_RugbyPR", "gloucester-rugby"),
"Premiership Rugby" -> WidgetDB(293093967174696960L, "Follow_RugbyPR", "premiership-rugby"),
"Exeter Chiefs" -> WidgetDB(293093389975560193L, "Follow_RugbyPR", "exeter-chiefs"),
"Bath" -> WidgetDB(292713176934522882L, "Follow_RugbyPR", "bath")
  )
        
    mapToData("avivaPremiership", premTeamFiles, premWidgets)
  }
  
  
    
    
  
  def generalTeamConsume(name: String, data: InputStream, widget: WidgetDB) = {
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
    } yield PlayerDB(playerName, twitter)
    
    TeamDB(name, teamTwitterHandle, link, players, widget)
  }
  
  private def mapToData(folderName: String, teamFiles: Seq[(String, String)], teamWidgets: Map[String, WidgetDB]) = {
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