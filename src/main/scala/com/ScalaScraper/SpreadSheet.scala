package com.ScalaScraper

import java.util.{Date, Properties}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{MenuButton, MenuItem}
import scalafx.scene.layout.VBox
import scalafx.scene.control.Label
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.beans.property.ObjectProperty 
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.geometry.Pos

import scala.io.Source
import collection.JavaConverters._
import org.jsoup.nodes.Element
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import com.github.tototoshi.csv._

object TypeClass {
  def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]
}

object Spreadsheets {//extends JFXApp {
  import com.ScalaScraper.ScrapeUtils._
  import com.ScalaScraper.EmailUtils._
  
  private[this] var textLinkList : List[Tuple2[String,String]] = _
  private[this] var csvWriter :CSVWriter= _
  private[this] var emailDate :Date = _

  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"))  //Not-Crazy-Big Fundings
  private[this] final val excludeHeaders: List[Header] = List(Header("Exits"),Header("People"),Header("Sponsored By"),Header("Jobs"),Header("Essential Reads"))
  private[this] final val htlmlUncheckedNames:Header = Header("Big-But-Not-<em>Crazy</em>-Big Fundings")

/*   stage = new PrimaryStage {
    scene = new Scene(200, 200) {
      content = new VBox {
        padding = Insets(10)
        spacing = 10
        autosize()
        children = List(
          new Label("StrictlyVC Email Scraper"){
            textFill = Color.DARKCYAN    
          },
          new MenuButton("MenuButton 1") {
            items = List(
              new MenuItem("MenuItem A") {
                onAction = {ae: ActionEvent => {println(ae.eventType + " occurred on Menu Item A")}}
              },
              new MenuItem("MenuItem B")
            )
          },
          new MenuButton {
            text = "MenuButton 2"
            items = List(
              new MenuItem("MenuItem C") {
                onAction = {ae: ActionEvent => {println(ae.eventType + " occurred on Menu Item C")}}
              },
              new MenuItem("MenuItem D")
            )
          },
          new Text("ΠΩΠΩ ΕΝΑ CSV!ΕΛΑ ΓΙΩΡΓΗ!") {
            font = Font.font(null, FontWeight.Bold, 18)
            fill = Color.DARKCYAN
            alignmentInParent = Pos.BottomCenter
          }
        )
      }
    }
  } */
  def main(args: Array[String]): Unit = {
    val code :String  = """Massive Fundings
    Arranta Bio, a months-old, Watertown, Ma.-based contract development manufacturing organization focused on the biotech industry, has raised $82 million in funding, including from Ampersand Capital Partners and Thermo Fisher. More here.
    B8ta, a four-year-old, San Francisco- and New York-based operator of an experiential retail-as-a-service platform, has raised $50 million in Series C funding led by Evolution Ventures, with participation from Macy’s, Khosla Ventures, and Peak State Ventures. Retail Touchpoints has more here.
    Tmunity Therapeutics, a four-year-old, Philadelphia, Pa.-based biotherapeutics company trying to propel a series of cancer-fighting cellular therapies through clinical trials, has raised $75 million in Series B funding led by Andreessen Horowitz. Other investors in the round include Kleiner Perkins, Westlake Village BioPartners, Gilead Sciences, The University of Pennsylvania, Be The Match BioTherapies and BrightEdge Venture Fund. More here and here.
    Vindex, a months-old, New York-based esports infrastructure platform founded by a former Activision Blizzard vice president, has raised $60 million in Series A funding from unnamed investors. ESPN has more here.
    Big-But-Not-Crazy-Big Fundings 
    Crunchbase, a 12-year-old, Bay Area-based private company information database that spun out of TechCrunch in 2015, has raised $30 million in Series C funding led by OMERS Ventures. Earlier investors also participated in the round, including Emergence Capital, Mayfield, Cowboy Ventures, and Verizon Ventures. TechCrunch has more here.
    CytoVale, a six-year-old, San Francisco-based med-tech startup focused on re-imagining diagnostics using cell mechanics and machine learning and applying this first to sepsis, just raised $15 million in funding. Breakout Ventures and Blackhorn Ventures led the round. More here.
    dotData, a year-old, San Mateo, Ca.-based data science automation platform, has raised $23 million in Series A funding led by JAFCO, with participation from Goldman Sachs and NEC Corp. VentureBeat has more here.
    Freetrade, a four-year-old, London-based startup offering a mobile app for zero-commission trades and professional advice, has raised $15 million in Series A funding, including from Draper Esprit. TechCrunch has more here.
    Smaller Fundings 
    Capital, a year-old, New York-based venture debt lender, has raised $5 million from Greycroft, Future Ventures, Wavemaker Ventures, and Disruptive. TechCrunch has more here.
    Inne, a three-year-old, Berlin, Germany-based maker of mini-labs for women to track hormones and fertility, has raised €8 million in Series A funding. Blossom Capital led the round, joined by Monkfish Equity. TechCrunch has more here.
    Kangarootime, a four-year-old, Buffalo, N.Y.-based maker of early childhood software, just raised $3.5 million in Series A funding, including from Cultivation Capital. More here.
    Kubit, a year-old, Fremont, Ca.-based maker of augmented analytics software that aims to help teams meet their objectives, has raised $4.5 million in seed funding from Shasta Ventures.
    Modus, a nearly two-year-old, Seattle-based real estate startup focused on title and escrow services, has raised $12.5 million in Series A funding co-led by NFX and Felicis Ventures. Liquid 2 Ventures and earlier backers Mucker Capital, Hustle Fund, 500 Startups, Rambleside and Cascadia Ventures also participated in the round. TechCrunch has more here.
    Tactile Mobility, a seven-year-old, Haifa, Israel-based auto tech firm that sells tactile sensing and data analytics tech for smart and autonomous vehicles, municipalities, and fleet managers, has raised $9 million in funding. Investors include Porsche and Union Tech Ventures. VentureBeat has more here.
    Techtonic, a 13-year-old, Boulder, Co.-based developer of onshore, product-driven software, has raised $6 million in Series B funding. Camden Partners led the round, joined by University Ventures and Zoma Capital. More here.
    People
    Aaref Hilaly has left Sequoia Capital after seven years to join Wing Venture Capital as a partner. Before joining Sequoia, Hilaly was a VP of engineering at Symantec.
    Scott Painter, the founder of the SoftBank-backed car leasing startup Fair.com, has resigned as CEO following last week’s layoffs and announcement of a planned restructuring, according to The Verge. SoftBank’s Adam Hieber is taking over as interim CEO. Painter will stay on as chairman of Fair. His brother, who was Fair’s CFO, left the company last week. Painter is insisting he'll remain highly involved and that "different skill sets are needed during different phases of a company's growth," but this still looks awfully messy and it certainly comes at a lousy time for SoftBank. In fact . . .
    SoftBank Masayoshi Son has evidently been so overworked of late that he actually fell asleep on stage earlier this week at the Future Investment Initiative conference, held in Saudi Arabia. We get it. Jet lag plus four people on a panel? It's a nightmare scenario. Business Insider has more here.
    VC Cack Wilhelm has left her role as a San Francisco-based partner with Accomplice, says Axios, a job she took two years ago after spending three years as a principal with Scale Venture Partners. Axios notes that Wilhelm isn't talking next steps yet.
    Noah Wintroub, a key figure at in JPMorgan who has been helping break the lock that Goldman Sachs and Morgan Stanley have on leading tech IPOs, receives more attention than he might like in a new Business Insider piece. The outlet talked with current and former JPMorgan employees about Wintroub's rise. While some suggest he's an "outstanding banker," he's also assigned some of the blame for JPMorgan's overly cozy relationship with WeWork. More here.
    """ 
    csvWriter = CSVWriter.open("/home/stkat/Downloads/output.csv" , append = true)
    csvWriter.writeRow(List("Name :", "Age :","Based :","Value_proposition :","Investment_amount :", "investment_round :","lead_VCs :","linkText :","hrefLinks :","Date :")) //,"rest_VCs :" after lead VC's

    emailDate = new Date()
    val filename = "/home/stkat/Desktop/Mwh.html"
    val fileContents = Source.fromFile(filename).getLines.mkString
    //println(fileContents)
    println(bodyMessageFilteringToCSVRow(fileContents))
    csvWriter.close()
  }


  private[this] def bodyMessageFilteringToCSVRow(bodyMessage: String):Unit = headerNamesIterator(bodyMessage,headerNames)

  private[this] def headerNamesIterator(bodyMessage: String, remainingNames: List[Header]):List[String] =
    remainingNames match {
      
      case  x :: xs =>
          textLinkList = List()
          val rawBodyMessage = org.jsoup.Jsoup.parse(bodyMessage).text()
          //println(rawBodyMessage)
          val nextHeader:Either[String,Header] = findNextHeader(xs,rawBodyMessage)
          val nextUncheckedHeader:Either[String,Header] = findNextHeader(excludeHeaders,rawBodyMessage)
          //println("nextHeader :"+nextHeader+"\t and \t xs:"+xs+"\t and x: \t"+x)
          if(rawBodyMessage.indexOf(x.name.trim).!=(-1) && nextHeader.isRight){
 
             val nextHeaderResult = nextHeader.fold(l => "NotFound", r => r.name.trim)
             val startingHeaderPlusLength = rawBodyMessage.indexOf(x.name.trim)+x.name.length
             val startingHeaderPlusLengthHtml = bodyMessage.indexOf(x.name.trim)+x.name.length

             val chunkedText = rawBodyMessage.substring(startingHeaderPlusLength,rawBodyMessage.indexOf(nextHeaderResult))

             val chunkedHtmlText = if(nextHeaderResult.equals("Big-But-Not-Crazy-Big Fundings") && bodyMessage.indexOf(nextHeaderResult) == -1)
               bodyMessage.substring(startingHeaderPlusLengthHtml,bodyMessage.indexOf(htlmlUncheckedNames.name))
             else
               bodyMessage.substring(startingHeaderPlusLengthHtml,bodyMessage.indexOf(nextHeaderResult))  //
             
             textLinkList = org.jsoup.Jsoup.parse(chunkedHtmlText).select("a").asScala.toList
             .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
             .filter{case (text,href) => text.contains("here")}.reverse
             headerContentFilter(chunkedText,true) :: headerNamesIterator(bodyMessage.substring(startingHeaderPlusLength,bodyMessage.length),xs)
          }else if(rawBodyMessage.indexOf(x.name.trim).!=(-1) && (xs == Nil || nextHeader.isLeft)){
             textLinkList = org.jsoup.Jsoup.parse(bodyMessage).select("a").asScala.toList
             .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
             .filter{case (text,href) => text.contains("here")}.reverse
             val processedBody = rawBodyMessage.substring(rawBodyMessage.indexOf(x.name.trim),rawBodyMessage.indexOf(nextUncheckedHeader.fold(l => "NotFound", r => r.name.trim)))
            headerContentFilter(processedBody,true)
            Nil
          } else {
            headerNamesIterator(bodyMessage, xs)
          }
      case Nil => Nil
    }

    private[this] def headerContentFilter(headerContents: String, firstTime : Boolean): String  = {
      headerContents.split("\\n").filter(headerContents => headerContents.trim.length != 0).foreach({ headerContent =>
        var lastIndex = headerContent //Get the un-HTML-ed code from headerContent
        //println("htmlheaderContents "+htmlheaderContents)
        var name =""
        if (lastIndex.indexOf(",") != -1) {
          name = lastIndex.slice(0, lastIndex.indexOf(","))
          lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.length)
        }
  
        val yearKeywords :List[String] = List("years-old","year-old","month-old","months-old","days-old")
        val yearIndexFound = calculateMinIndex(yearKeywords,lastIndex)
  
        var age =""
        if(yearIndexFound != "not_found"){
          age = lastIndex.slice(0, lastIndex.indexOf(yearIndexFound)+yearIndexFound.length)
          lastIndex = lastIndex.slice(lastIndex.indexOf(yearIndexFound)+yearIndexFound.length+1,lastIndex.length)
        }
  
        val basedKeywords :List[String] = List("-based","- based","based")
        val basedIndexFound = calculateMinIndex(basedKeywords,lastIndex)
  
        val preInvestmentAmountKeywords :List[String] = List("has raised","just raised","raised","raising","has closed on","has closed","closed")
        val preIAindexFound = calculateMinIndex(preInvestmentAmountKeywords,lastIndex)
  
        var based =""
        var valueProposition =""
        if(basedIndexFound !="not_found"){
          based = lastIndex.slice(0, lastIndex.indexOf(basedIndexFound)+basedIndexFound.length)
          lastIndex = lastIndex.slice(lastIndex.indexOf(basedIndexFound)+basedIndexFound.length,lastIndex.length)
  
          if(!preIAindexFound.equals("not_found")){
            valueProposition = lastIndex.slice(0, lastIndex.indexOf(preIAindexFound)-1)
            lastIndex = lastIndex.slice(lastIndex.indexOf(preIAindexFound),lastIndex.length)
          }else {
            valueProposition = lastIndex.slice(0, lastIndex.indexOf(",")-1)
            lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.length)
          }
        }else if(basedIndexFound =="not_found"){
          valueProposition = lastIndex.slice(0, lastIndex.indexOf(","))
          lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.length)
        }
  
        val preInvestmentRoundKeywords :List[String] = List("in pre-Series","in Series","in seed","from", "in funding") //,"in funding"
        val preInvestIndex = calculateMinIndex(preInvestmentRoundKeywords,lastIndex)
  
        val afterInvestmentRoundKeywords :List[String] = List("funding","in financing","financing","valuation")
        val indexFound3 = calculateMinIndex(afterInvestmentRoundKeywords,lastIndex)
  
        val prelinkKeywords :List[String] = List("More here and here","more here and here","has more here","has much more here","More here","here")  //".",
        val indexFound5 = calculateMinIndex(prelinkKeywords,lastIndex)
  
        //InvestedAmount
        var investmentAmount =""
        if(preInvestIndex != "not_found" && compareIndexes(indexFound5,preInvestIndex,lastIndex) && compareIndexes(indexFound3,preInvestIndex,lastIndex) ){
          investmentAmount = lastIndex.slice(0+lastIndex.indexOf(preIAindexFound)+preIAindexFound.length, lastIndex.indexOf(preInvestIndex))
          lastIndex = lastIndex.slice(lastIndex.indexOf(preInvestIndex),lastIndex.length)
        }else if(preInvestIndex == "not found" && indexFound3 != "not found" ) {
          investmentAmount = lastIndex.slice(0, lastIndex.indexOf(indexFound3))
          lastIndex = lastIndex.slice(lastIndex.indexOf(preInvestIndex),lastIndex.length)
        } else if(preInvestIndex != "not found" && indexFound3 != "not found"  && compareIndexes(indexFound5,preInvestIndex,lastIndex) ) {
          investmentAmount = lastIndex.slice(0+lastIndex.indexOf(preIAindexFound)+preIAindexFound.length, lastIndex.indexOf(indexFound3))
          lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3),lastIndex.length)
        }
  
        val preInvestorsKeywords :List[String] = List("led by","co-led by","from","include","led the round")  //".",
        val indexFound4 = calculateMinIndex(preInvestorsKeywords,lastIndex)
  
        //investmentRound
        var investmentRound =""
        if(indexFound3 != "not_found" && preInvestIndex != "not_found" && compareIndexes(indexFound4,preInvestIndex,lastIndex) && preInvestIndex =="in funding"){
          investmentRound = lastIndex.slice(0, lastIndex.indexOf(preInvestIndex)+preInvestIndex.length)
          lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3)+indexFound3.length,lastIndex.length)
        }else if(indexFound3 != "not_found" && preInvestIndex != "not_found" && compareIndexes(indexFound4,preInvestIndex,lastIndex) ){
          investmentRound = lastIndex.slice(0, lastIndex.indexOf(indexFound3))
          lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3)+indexFound3.length,lastIndex.length)
        }
  
        //Investors
        var investors =""
        if(indexFound4 != "not_found") {
          if (indexFound4 == "led the round" && indexFound5 != "not found"){
            investors = lastIndex.slice(0, lastIndex.indexOf(indexFound5))
            lastIndex = lastIndex.slice(lastIndex.indexOf(investors)+ investors.length, lastIndex.length)
          }else{
            investors = lastIndex.slice(0+lastIndex.indexOf(indexFound4)+indexFound4.length+1, lastIndex.indexOf(".",0+lastIndex.indexOf(indexFound4)+indexFound4.length+1))
            lastIndex = lastIndex.slice(lastIndex.indexOf(".",0+lastIndex.indexOf(indexFound4)+indexFound4.length+1), lastIndex.length)
          }
        }
  
        //Link
        var link =""
        if(indexFound5 != "not_found" && compareIndexes(yearIndexFound,indexFound5,lastIndex) ){
          val newAgeIndex = calculateMinIndex(yearKeywords,lastIndex)
          if(lastIndex.indexOf(newAgeIndex) != -1 && (compareIndexes(newAgeIndex,indexFound5,lastIndex))  ){
            link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.length)
            if(lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length).length >=0){
              headerContentFilter(lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length),true)
            }
          }else {
            link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.length)
            lastIndex = lastIndex.slice(0,lastIndex.indexOf(".")).appended("\n").slice(0,lastIndex.length).toString
          }
        }else{
          link ="@not_found"
          if(lastIndex.trim.length != 0 && firstTime)
            headerContentFilter(lastIndex,false)
          else
            link = lastIndex.slice(0, lastIndex.length) 
        }
        if(!link.equals("@not_found")){
          val occurences =link.toSeq.sliding("here".length).map(_.unwrap).count(occ => occ.==("here"))
          val linkResult =(0 until occurences).foldLeft(new StringBuilder(""))((acc,result) => acc.asInstanceOf[StringBuilder].addString(new StringBuilder("=HYPERLINK(\""+textLinkList.headOption.map(x => x._2).getOrElse("")+"\";\"here\")"))).toString //;\"here\"
          textLinkList = textLinkList.drop(occurences)
          val newList = List(name,age,based,valueProposition,investmentAmount,investmentRound,investors,link,linkResult,emailDate)
          csvWriter.writeRow(newList)
        }else{
          csvWriter.writeRow(List(name,age,based,valueProposition,investmentAmount,investmentRound,investors,"@not_found","@not_found",emailDate))
        }
  
      })
     ""
    }
  
  }