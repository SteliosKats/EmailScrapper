package scalamail

import java.util.{Date, Properties}

import com.github.tototoshi.csv._
import javax.mail._
import javax.mail.internet._
import org.jsoup.nodes.Element

import scala.annotation.tailrec
import scala.collection.JavaConverters._

import scalafx.application.JFXApp
import scalafx.scene.Scene
import javafx.scene.control.{ToggleButton => JfxToggleBtn}
import scalafx.geometry.Insets
import scalafx.scene.control.{Label, ToggleButton, ToggleGroup}
import scalafx.scene.control.ProgressBar
import scalafx.scene.layout.{StackPane, HBox, Priority, VBox}
import scalafx.scene.Scene
import scalafx.geometry.Insets
import scalafx.scene.control.ProgressIndicator
import scalafx.scene.input.KeyCode.G
import scalafx.scene.layout.GridPane
import scalafx.scene.control.TextField
import scalafx.scene.control.Button
import scalafx.geometry.{Insets, Pos}

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.geometry.{HPos, Insets, Pos}
import scalafx.scene.control.{Button, Label, Separator, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{ColumnConstraints, GridPane, Priority, RowConstraints, VBox}
import scalafx.scene.control.PasswordField


object TypeClass {
  def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]
}

object EmailScraperGui extends JFXApp {

  val toggleLabel = new Label {
    text ="Scrape Emails"
    style = "-fx-font-size: 2em;"
  }    // Radio Button Toggle Group



  stage = new JFXApp.PrimaryStage {
    title = "Email Scraper v1.0"
    scene = new Scene {
      root = {

        val labelEmail = new Label {
          text = "Email :"
          alignmentInParent = Pos.BaselineLeft
        }
        GridPane.setConstraints(labelEmail, 0, 0, 1, 1)

        val textFieldEmail = new TextField {
          promptText = "Type email..."
          alignmentInParent = Pos.BaselineRight
        }
        GridPane.setConstraints(textFieldEmail, 1, 0, 15, 1)

        val labelIMAPPass = new Label {
          text = "IMAP Password :"
          alignmentInParent = Pos.BaselineLeft
        }
        GridPane.setConstraints(labelIMAPPass, 0, 1, 1, 1)

        val textFieldIMAPPass = new PasswordField {
          promptText = "Type IMAP password..."
          alignmentInParent = Pos.BaselineRight
        }
        GridPane.setConstraints(textFieldIMAPPass, 1, 1, 15, 1)

        val labelSavedPath = new Label {
          text = "Output Path :"
          alignmentInParent = Pos.BaselineLeft
        }
        GridPane.setConstraints(labelSavedPath, 0, 2, 1, 1)

        val textSavedPath = new TextField {
          promptText = "Type full path..."
          alignmentInParent = Pos.BaselineLeft
        }
        GridPane.setConstraints(textSavedPath, 1, 2, 15, 1)

        val progressInd = new ProgressIndicator {
          prefWidth = 50
          prefHeight = 50
          disable =true
          visible = false
          //progress = 1.0F
        }
        GridPane.setConstraints(progressInd, 2, 3, 2, 1)

        val tog = new ToggleGroup {
          selectedToggle.onChange(
            (_, oldValue, newValue) => {
              progressInd.visible_=(true)
              toggleLabel.text = "Beggining Scraping Emails : " + newValue.asInstanceOf[JfxToggleBtn].getText
              stage.setTitle("Scraping Progress Bar")
              stage.sizeToScene()
              stage.centerOnScreen()
              val emailText = "--email "+textFieldEmail.getText()
              val imapPassText = "--imapsPass "+textFieldIMAPPass.getText()
              val savedPathText = "--outputPath "+textSavedPath.getText()
              ScalaImapSsl.main(Array(emailText,imapPassText,savedPathText))
            }
          )
        }

        val buttonEmailScrape = new ToggleButton {
          text = "Scrape Emails..."
          alignmentInParent = Pos.BaselineCenter
          toggleGroup = tog
        }
        GridPane.setConstraints(buttonEmailScrape, 0, 3, 1, 1)

        new GridPane {
          hgap = 6
          vgap = 6
          margin = Insets(18)
          children ++= Seq(labelEmail, textFieldEmail, labelIMAPPass, textFieldIMAPPass,labelSavedPath,textSavedPath,buttonEmailScrape,progressInd)
        }

     
      }
    }
  }
}

object ScalaImapSsl {

  import com.ScalaScraper.EmailUtils._
  import com.ScalaScraper.JCommanderArgs._
  import com.ScalaScraper.ScrapeUtils._
  private[this] final val waitTimeout = 2000
  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings"), Header("Big-But-N0t-Crazy-Big Fundings")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much "),Header("Not-Telling-How-Much Fundings"),Header("New Funds"))  //Not-Crazy-Big Fundings //Not-Saying-How-Much Fundings
  private[this] final val excludeHeaders: List[Header] = List(Header("Exits</span>"),Header("IPOs</span>"),Header("People</span>"),Header("Sponsored By</span>"),Header("Jobs</span>"),Header("Essential Reads</span>"))
  private[this] final val htlmlUncheckedNames: List[(String,String)] = List("Big-But-Not-Crazy-Big Fundings"->"Big-But-Not-<em>Crazy</em>-Big Fundings","Not-Telling-How-Much Fundings" -> "Not-Telling-How-Much&nbsp;Fundings","Smaller Fundings"->"Smaller&nbsp;Fundings&nbsp;")

  //private[this] final val outputFile = new BufferedWriter(new FileWriter("/home/stelios/Downloads/output.csv"))
  private[this] var csvWriter :CSVWriter= _
  private[this] var emailDate :Date = _
  private[this] var textLinkList : List[Tuple2[String,String]] = _
  private[this] final val scrapeFolderName = "scrapedEmails"
  private[this] final val georgeAddr: InternetAddress = new InternetAddress(
    "gk@venturefriends.vc")
  private[this] final val ventureAddr: InternetAddress = new InternetAddress(
    "StrictlyVC <connie@strictlyvc.com>")

  def main(args: Array[String]): Unit = {
      parser1.parse(args, Config()) match {
      case Some(config) =>
        csvWriter = CSVWriter.open(config.outputPath+"output.csv" , append = true)  //"/home/stelios/Downloads/output.csv"
        val props: Properties = System.getProperties
        props.setProperty("mail.store.protocol", "imaps")
        val session: Session = Session.getDefaultInstance(props, null)
        val store = session.getStore("imaps")
        try {
            Thread.currentThread.setContextClassLoader(this.getClass.getClassLoader) // Bypass exception "IMAPInputStream cannot be cast to javax.mail.Multipart"
            import javax.activation.CommandMap
            import javax.activation.MailcapCommandMap
            val mc = CommandMap.getDefaultCommandMap.asInstanceOf[MailcapCommandMap]
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
            CommandMap.setDefaultCommandMap(mc)
            store.connect("imap.gmail.com",
              config.email,
              config.imapsPass)
            val inbox = store.getFolder("StrictlyVC")
            inbox.open(Folder.READ_WRITE)

            val scrapeFolder = store.getFolder(scrapeFolderName)
            if (!scrapeFolder.exists()) createFolder(scrapeFolderName,store)
            val messages = inbox.getMessages()

            val filteredMessages = messages.filter(message => message.getFrom.contains(ventureAddr))  //ventureAddr
            csvWriter.writeRow(List("Name :", "Age :","Based :","Value_proposition :","Investment_amount :", "investment_round :","lead_VCs :","link","hrefLink(s)","Date")) //,"rest_VCs :" after lead VC's

            for(message:Message <- filteredMessages ) {
              if (message.isMimeType("multipart/*") ){  //!emailStr.equals("") &&
                val result = getTextFromMimeMultipart(message.getContent.asInstanceOf[MimeMultipart])
                emailDate = message.getReceivedDate
                bodyMessageFilteringToCSVRow(result)
                textLinkList = Nil
              }
            }
            csvWriter.close()
            scrapeFolder.open(Folder.READ_WRITE)
            inbox.copyMessages(filteredMessages,scrapeFolder)
            waitForMoveCompletion(scrapeFolder, waitTimeout)
            filteredMessages.foreach(message => message.setFlag(Flags.Flag.DELETED, true))
            scrapeFolder.close(true)
            inbox.close(true)
            } catch {
              case e: NoSuchProviderException =>
                e.printStackTrace()
                System.exit(1)
              case me: MessagingException =>
                me.printStackTrace()
                System.exit(2)
            } finally {
              store.close()
            }
      case _ =>
              // arguments are bad, error message will have been displayed
      }
  }

  @tailrec
  private[this] def listIterator(list: List[Address],georgeAddr: Address): String = list match {
    case List()                                     => ""
    case (head :: Nil) if (head.equals(georgeAddr)) => georgeAddr.toString
    case head :: rest                               => listIterator(rest, georgeAddr)
  }

  private[this] def getTextFromMimeMultipart(mimeMultipart: MimeMultipart):String = {
    val count:Int = mimeMultipart.getCount()
    yieldContentResult("",mimeMultipart).apply(count-1)
  }

  private[this] def yieldContentResult(result: String,mimeMultipart: MimeMultipart):(Int => String) = {
    case x if(x>= 0) =>
      val bodypart = mimeMultipart.getBodyPart(x)
      if(bodypart.isMimeType("text/plain"))
        yieldContentResult(result + "\n" + bodypart.getContent(),mimeMultipart)(x-1)
      if(bodypart.isMimeType("text/html")){
       yieldContentResult(result + bodypart.getContent.asInstanceOf[String],mimeMultipart)(x-1)
      }else if (bodypart.getContent.isInstanceOf[MimeMultipart])
        ""
      else
        result
    case _ => result
  }

  private[this] def bodyMessageFilteringToCSVRow(bodyMessage: String):Unit = headerNamesIterator(bodyMessage,headerNames)

  private[this] def headerNamesIterator(bodyMessage: String, remainingNames: List[Header]):List[String] =
    remainingNames match {

      case  x :: xs =>
          textLinkList = List()
          val rawStrBodyMessage = org.jsoup.Jsoup.parse(bodyMessage).text()  //text in string format  un-htmled
          val nextHeaderName:Either[String,Header] = findNextHeader(xs,rawStrBodyMessage)
          val nextUncheckedHeader:Either[String,Header] = findNextHeader(excludeHeaders,bodyMessage)
          if(rawStrBodyMessage.indexOf(x.name.trim).!=(-1) && nextHeaderName.isRight){

             val nextHeader = nextHeaderName.fold(l => "NotFound", r => r.name.trim)
             val startingHeaderPlusLength = rawStrBodyMessage.indexOf(x.name.trim)+x.name.length
             val startingHeaderPlusLengthHtml = bodyMessage.indexOf(x.name.trim)+x.name.length
             val chunkedRawText = rawStrBodyMessage.substring(startingHeaderPlusLength,rawStrBodyMessage.indexOf(nextHeader))
             val nextHeaderFoundOnList :List[String] = htlmlUncheckedNames.map{case (rawHeader,htmlHeader) => if(nextHeader.equals(rawHeader)) htmlHeader}.filter(_ != (())).asInstanceOf[List[String]]
             val chunkedHtmlText = if(!nextHeaderFoundOnList.isEmpty && bodyMessage.indexOf(nextHeaderFoundOnList.head) != -1)
               bodyMessage.substring(startingHeaderPlusLengthHtml,bodyMessage.indexOf(nextHeaderFoundOnList.head))
             else
               bodyMessage.substring(startingHeaderPlusLengthHtml,
                          bodyMessage.indexOf(nextHeader))

              textLinkList = org.jsoup.Jsoup.parse(chunkedHtmlText).select("a").asScala.toList
             .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
             .filter{case (text,href) => text.contains("here")}.reverse
             headerContentFilter(chunkedRawText,true) :: headerNamesIterator(bodyMessage.substring(startingHeaderPlusLength,bodyMessage.length),xs)
          } else if(rawStrBodyMessage.indexOf(x.name.trim).!=(-1) && (xs == Nil || nextHeaderName.isLeft)){  //case when the last header is found, so the remaining text does not contain a valid header
            textLinkList = org.jsoup.Jsoup.parse(bodyMessage).select("a").asScala.toList
             .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
             .filter{case (text,href) => text.contains("here")}.reverse
             val startingHeaderPlusLengthHtml = bodyMessage.indexOf(x.name.trim)+x.name.length
             val processedBody = org.jsoup.Jsoup.parse(bodyMessage.substring(startingHeaderPlusLengthHtml,bodyMessage.indexOf(nextUncheckedHeader.fold(l => "NotFound", r => r.name.trim)))).text()
            headerContentFilter(processedBody,true) :: headerNamesIterator("",Nil)
          } else {
            headerNamesIterator(bodyMessage, xs)
          }
      case Nil => Nil
    }

    private[this] def headerContentFilter(headerContents: String ,firstTime: Boolean): String  = {
      headerContents.split("\\n").filter(headerContents => headerContents.trim.length != 0).foreach({ headerContent =>
        var lastIndex = headerContent //Get the un-HTML-ed code from headerContent

        //Name
        var name =""
        if (lastIndex.indexOf(",") != -1) {
          name = lastIndex.slice(0, lastIndex.indexOf(","))
          lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.length)
        }

        val yearKeywords :List[String] =List("years-old","year-old","month-old","months-old","days-old")
        val yearIndexFound = calculateMinIndex(yearKeywords,lastIndex)

        //Age
        var age =""
        if(yearIndexFound != "not_found"){
          age = lastIndex.slice(0, lastIndex.indexOf(yearIndexFound)+yearIndexFound.length)
          lastIndex = lastIndex.slice(lastIndex.indexOf(yearIndexFound)+yearIndexFound.length+1,lastIndex.length)
        }

        val basedKeywords :List[String] =List("-based","- based","based")
        val basedIndexFound = calculateMinIndex(basedKeywords,lastIndex)

        val preInvestmentAmountKeywords :List[String] =List("has raised","just raised","raised","raising","has closed on","has closed","closed")
        val preIAindexFound = calculateMinIndex(preInvestmentAmountKeywords,lastIndex)

        val prelinkKeywords :List[String] =List("More here and here","more here and here","has more here","has much more here","More here","here")  //".",
        val indexFound2 = calculateMinIndex(prelinkKeywords,lastIndex)

        var based =""
        var valueProposition =""
        if(basedIndexFound !="not_found" && compareIndexes(indexFound2,basedIndexFound,lastIndex) ){
          based = lastIndex.slice(0, lastIndex.indexOf(basedIndexFound)+basedIndexFound.length)
          lastIndex = lastIndex.slice(lastIndex.indexOf(basedIndexFound)+basedIndexFound.length,lastIndex.length)

          if(!preIAindexFound.equals("not_found") && compareIndexes(indexFound2,preIAindexFound,lastIndex) ){
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
     
        val indexFound5 = calculateMinIndex(prelinkKeywords,lastIndex)

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
        if(indexFound5 != "not_found" && compareIndexes(yearIndexFound,indexFound5,lastIndex) ){
          val newAgeIndex = calculateMinIndex(yearKeywords,lastIndex)
          if(lastIndex.indexOf(newAgeIndex) != -1 && (compareIndexes(newAgeIndex,indexFound5,lastIndex))  ){
            link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.length)
            if(lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length).length >=0){
              headerContentFilter(lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length),false)
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