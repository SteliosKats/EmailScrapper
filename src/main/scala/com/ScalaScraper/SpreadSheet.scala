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

  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not ")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"))  //-Crazy-Big Fundings
  

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
    Faire, a nearly three-year-old, San Francisco-based curated wholesale marketplace that connects independent retailers and makers, has raised $150 million in Series D funding at a $1 billion valuation. Lightspeed Venture Partners and Founders Fund co-led the round, joined by including earlier backers Forerunner Ventures, YC Continuity, and Khosla Ventures. Crunchbase News has more here.
    Big-But-Not-Crazy-Big Fundings 
    Brut, a 3.5-year-old, Paris-based video news startup that's focused on social good and social impact, has raised $40 million in Series B funding, money it will use, in part, to launch in the U.S. The round was led by Red River West and blisce. Other investors include Aryeh Bourkoff, the founder and CEO of LionTree; and Eric Zinterhofer, a founding partner of Searchlight Capital Partners. TechCrunch has more here.
    Datameer, a 10-year-old, San Francisco-based big data analytics platform, has raised $40 million in funding led by ST Telemedia, with participation from earlier backers Redpoint Ventures, Kleiner Perkins, Nextworld Capital, Citi Ventures, and Top Tier Capital Partners. TechCrunch has more here. 
    Duality, a three-year-old, Newark, N.J.-based company that builds software based on homomorphic encryption — a technique that encrypts an organization’s data in a way that lets it stay encrypted even as the company collaborates with third parties that also process the data — has raised $16 million in funding. The Series A round is being led by Intel Capital, with participation from Hearst Ventures and Team8. TechCrunch has more here.
    ElasticRun, a three-year-old Pune, India-based startup that's building a logistics network to supply goods to the thousands of neighborhood stores that dot large and small cities, towns, and villages in India, has $40 million in a Series C funding led by Prosus Ventures, the newly public company spun out of Naspers. Earlier investors Avataar Ventures and Kalaari Capital also participated in the round, which brings ElasticRun's total funding to $55.5 million. TechCrunch explains the opportunity it's chasing here.
    Mirror, a three-year-old, New York-based fitness content streaming device that users can hang and use like a traditional full-length mirror, has raised $34 million more in a Series B-1 funding round led by Point72 Ventures. , whose founder, hedge fund billionaire Steve Cohen, is also joining the board. The company has now raised $72 million to date. More here.
    Muy, a two-year-old, Bogota, Colombia-based dark kitchen company that's producing its own food, has raised $15 million in Series B funding to expand into Mexico and, later, Brazil. The Mexico-based investor ALLVP led the round, with participation from earlier backer Seaya. The company has now raised $20.5 million altogether. TechCrunch has more here.
    Particle, a seven-year-old, San Francisco-based platform for Internet of Things devices, has raised $40 million in its latest round of funding. Qualcomm Ventures and Energy Impact Partners led the round, with participation from earlier investors Root Ventures, Bonfire Ventures, Industry Ventures, Spark Capital, Green D Ventures, Counterpart Ventures, and SOSV. The company has now raised $81 million to date. TechCrunch has more here. 
    Rokt, a seven-year-old, New York-based  e-commerce marketing technology, today announced an investment of $48 million from global investment firm, TDM Growth Partners, as well as existing investors. More here. 
    Stampli, a four-year-old, Mountain View, Ca.-based company looking to automate invoice management, has today announced the close of a $25 million Series B round. The funding was led by SignalFire, with participation from Hillsven Capital, Bloomberg Beta, as well as new investor NextWorld Capital. TechCrunch has more here.
    Smaller Fundings 
    Artmyn, a three-year-old, Saint-Sulpice, Switzerland-based startup focused on the highly accurate digitization of visual artworks (it has developed a portable scanner that captures gigabytes of data describing the artwork in its finest details that it says results in true-to-life a visualization), has raised $4 million in funding. The round was co-led by the online marketplace for fine art and antiques Invaluable, along with earlier backers. More here.
    CoinList, a two-year-old, San Francisco-based company that connects investors to crypto projects, has raised $10 million in funding led by Polychain Capital, with participation from Jack Dorsey, among others. The WSJ has more here.
    Link3D, a three-year-old, New York-based startup behind an enterprise additive manufacturing execution system that connects organizations' digital manufacturing ecosystems, has raised $7 million in funding led by the AI-focused venture capital firm AI Capital. More here.  
    Ruti, a nearly 11-year-old, Belmont, Ca.-based fashion brand with what it describes as a high-tech, highly personalized in-store experience (it has eight stores so far), has raised $6 million in a Series A funding led by Viola Ventures. More here.
    Stardog, a 12-year-old, Arlington, Va.-based reusable, scalable knowledge graph platform that helps enterprises unify all their data, has raised $9 million in Series B funding led by Tenfore Holdings, with participation from Grotech Ventures, Boulder Ventures, and Core Capital. More here. 
    Workiz, a four-year-old, San Diego, Ca.-based startup whose software helps field service professionals manage their work, has raised $5 million in Series A funding led by Magenta Venture Partners, with participation from earlier investor Aleph. TechCrunch has more here.
    Jungle Ventures founded seven years ago in Singapore, has raised $240 million for its third Southeast Asian fund, with almost 60 percent of the capital coming from outside Asia, says Bloomberg. Investors in the fund included German development finance institution DEG, the World Bank‘s International Finance Corp., Bangkok Bank’s corporate venture capital arm, Cisco Investments and Singapore’s state investment firm, Temasek Holdings. More here.
    New Funds 
    """ 
    csvWriter = CSVWriter.open("/home/stkat/Downloads/output.csv" , append = true)
    csvWriter.writeRow(List("Name :", "Age :","Based :","Value_proposition :","Investment_amount :", "investment_round :","lead_VCs :","link","Date")) //,"rest_VCs :" after lead VC's

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
      case body :: Nil => List[String]()
      case  x :: xs =>
        val rawBodyMessage = org.jsoup.Jsoup.parse(bodyMessage).text()
        val nextHeader:Either[String,Header] =findNextHeader(xs,rawBodyMessage)

        if(rawBodyMessage.indexOf(x.name).!=(-1) && nextHeader.isRight){
          val chunkedHtmlText = bodyMessage.slice(bodyMessage.indexOf(x.name.trim)+x.name.trim.length,bodyMessage.indexOf(nextHeader.fold(l => "NotFound", r => r.name.trim)))  //
          textLinkList = org.jsoup.Jsoup.parse(chunkedHtmlText).select("a").asScala.toList
          .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
          .filter{case (text,href) => text.contains("here")}

          headerContentFilter(chunkedHtmlText,org.jsoup.Jsoup.parse(chunkedHtmlText).text(),x.name) :: headerNamesIterator(chunkedHtmlText,xs)
        }else
          List[String]()
      case Nil => List[String]()
    }


    private[this] def headerContentFilter(htmlheaderContents: String, headerContents: String, headerName: String): String = {
      headerContents.split("\\n").filter(htmlheaderContents => htmlheaderContents.trim.length != 0).foreach({ headerContent =>
        var lastIndex = headerContent //Get the un-HTML-ed code from headerContent
        //println("htmlheaderContents "+htmlheaderContents)
        var name =""
        if (lastIndex.indexOf(",") != -1) {
          name = lastIndex.slice(0, lastIndex.indexOf(","))
          lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.length)
        }
  
        val yearKeywords :List[String] =List("years-old","year-old","month-old","months-old","days-old")
        val yearIndexFound = calculateMinIndex(yearKeywords,lastIndex)
  
        var age =""
        if(yearIndexFound != "not_found"){
          age = lastIndex.slice(0, lastIndex.indexOf(yearIndexFound)+yearIndexFound.length)
          lastIndex = lastIndex.slice(lastIndex.indexOf(yearIndexFound)+yearIndexFound.length+1,lastIndex.length)
        }
  
        val basedKeywords :List[String] =List("-based","- based","based")
        val basedIndexFound = calculateMinIndex(basedKeywords,lastIndex)
  
        val preInvestmentAmountKeywords :List[String] =List("has raised","just raised","raised","raising","has closed on","has closed","closed")
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
  
        val preInvestmentRoundKeywords :List[String] =List("in pre-Series","in Series","in seed","from", "in funding") //,"in funding"
        val preInvestIndex = calculateMinIndex(preInvestmentRoundKeywords,lastIndex)
  
        val afterInvestmentRoundKeywords :List[String] =List("funding","in financing","financing","valuation")
        val indexFound3 = calculateMinIndex(afterInvestmentRoundKeywords,lastIndex)
  
        val prelinkKeywords :List[String] =List("has more here","has much more here","More here","here")  //".",
        val indexFound5 = calculateMinIndex(prelinkKeywords,lastIndex)
  
        //println("indexFound: "+indexFound+" and preInvestIndex: "+preInvestIndex+" and lastIndex.indexOf(preInvestIndex) "+lastIndex.indexOf(preInvestIndex) +" must be < indexFound3 :"+indexFound3+" which is lastIndex.indexOf(indexFound3)"+lastIndex.indexOf(indexFound3))
        //InvestedAmount
        var investmentAmount =""
        if(preInvestIndex != "not_found" && compareIndexes(indexFound5,preInvestIndex,lastIndex) && compareIndexes(indexFound3,preInvestIndex,lastIndex) ){
          investmentAmount = lastIndex.slice(0+lastIndex.indexOf(preIAindexFound)+preIAindexFound.length, lastIndex.indexOf(preInvestIndex))
          lastIndex = lastIndex.slice(lastIndex.indexOf(preInvestIndex),lastIndex.length)
        }else if(preInvestIndex == "not found"  && indexFound3 != "not found" ) {
          investmentAmount = lastIndex.slice(0, lastIndex.indexOf(indexFound3))
          lastIndex = lastIndex.slice(lastIndex.indexOf(preInvestIndex),lastIndex.length)
        } else if(preInvestIndex != "not found"  && indexFound3 != "not found"  && compareIndexes(indexFound5,preInvestIndex,lastIndex) ) {
          investmentAmount = lastIndex.slice(0+lastIndex.indexOf(preIAindexFound)+preIAindexFound.length, lastIndex.indexOf(indexFound3))
          lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3),lastIndex.length)
        }
  
        val preInvestorsKeywords :List[String] =List("led by","co-led by","from","include","led the round")  //".",
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
        if(indexFound5 != "not_found"){
          val newAgeIndex = calculateMinIndex(yearKeywords,lastIndex)
          if(lastIndex.indexOf(newAgeIndex) != -1 && (compareIndexes(newAgeIndex,indexFound5,lastIndex))  ){
            link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.length)
            if(lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length).length >=0){
              headerContentFilter(htmlheaderContents,lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length),headerName)
            }
          }else {
            link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.length)
            lastIndex = lastIndex.slice(0,lastIndex.indexOf(".")).appended("\n").slice(0,lastIndex.length).toString
          }
        }else{
          link ="@not_found"
        }
        if(!link.equals("@not_found")){
          val occurences =link.toSeq.sliding("here".length).map(_.unwrap).count(occ => occ.==("here"))
          val linkResult =(0 until occurences).foldLeft(new StringBuilder(""))((acc,result) => acc.asInstanceOf[StringBuilder].addString(new StringBuilder("=HYPERLINK(\""+textLinkList.headOption.getOrElse("")+"\",\"here\")"))).toString
          textLinkList = textLinkList.drop(occurences)
          val newList = List(name,age,based,valueProposition,investmentAmount,investmentRound,investors,link,linkResult,emailDate)
          csvWriter.writeRow(newList)
        }else{
          csvWriter.writeRow(List(name,age,based,valueProposition,investmentAmount,investmentRound,investors,"",emailDate))
        }
  
      })
      ""
    }
  
  }