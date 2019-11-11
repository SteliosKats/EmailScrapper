package scalamail

import java.util.{Date, Properties}

import com.github.tototoshi.csv._
import javax.mail._
import javax.mail.internet._
import collection.JavaConverters._
import scala.annotation.tailrec

object TypeClass {
  def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]
}

object ScalaImapSsl {
  import com.ScalaScraper.EmailUtils._
  import com.ScalaScraper.JCommanderArgs._
  import com.ScalaScraper.ScrapeUtils._
  private[this] final val waitTimeout = 2000
  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings ")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"),Header("Exits"))

  //private[this] final val outputFile = new BufferedWriter(new FileWriter("/home/stelios/Downloads/output.csv"))
  private[this] var csvWriter :CSVWriter= _
  private[this] var emailDate :Date = _
  private[this] var hrefLinkList : List[String] = Nil
  private[this] final val scrapeFolderName = "scrapedEmails"
  private[this] final val georgeAddr: InternetAddress = new InternetAddress(
    "gk@venturefriends.vc")
  private[this] final val ventureAddr: InternetAddress = new InternetAddress(
    "StrictlyVC <connie@strictlyvc.com>")

  def main(args: Array[String]) {
      parser1.parse(args, Config()) match {
      case Some(config) =>
        csvWriter = CSVWriter.open(config.outputPath+"output.csv" , append = true)  //"/home/stelios/Downloads/output.csv"
        val props: Properties = System.getProperties
        props.setProperty("mail.store.protocol", "imaps")
        val session: Session = Session.getDefaultInstance(props, null)
        val store = session.getStore("imaps")
        try {
            Thread.currentThread.setContextClassLoader(this.getClass.getClassLoader) // Bypass exception "IMAPInputStream cannot be cast to javax.mail.Multipart"
            store.connect("imap.gmail.com",
              config.email,
              config.imapsPass)
            val inbox = store.getFolder("StrictlyVC")
            inbox.open(Folder.READ_WRITE)

            val scrapeFolder = store.getFolder(scrapeFolderName)
            if (!scrapeFolder.exists()) createFolder(scrapeFolderName,store)
            val messages = inbox.getMessages()

            val filteredMessages = messages.filter(message => message.getFrom.contains(ventureAddr))  //ventureAddr
            csvWriter.writeRow(List("Name :", "Age :","Based :","Value_proposition :","Investment_amount :", "investment_round :","lead_VCs :","link","Date")) //,"rest_VCs :" after lead VC's

            for(message:Message <- filteredMessages ) {
              if (message.isMimeType("multipart/*") ){  //!emailStr.equals("") &&
                val result = getTextFromMimeMultipart(message.getContent.asInstanceOf[MimeMultipart])
                emailDate = message.getReceivedDate
                bodyMessageFilteringToCSVRow(result)
                hrefLinkList = Nil
                println("hrefLinkList :"+hrefLinkList)
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
      else if(bodypart.isMimeType("text/html"))
       yieldContentResult(result + bodypart.getContent.asInstanceOf[String],mimeMultipart)(x-1)
      else if (bodypart.getContent.isInstanceOf[MimeMultipart])
        ""
      else
        result
    case _ => result
  }

  private[this] def bodyMessageFilteringToCSVRow(bodyMessage: String):Unit = headerNamesIterator(bodyMessage,headerNames)

  private[this] def headerNamesIterator(bodyMessage: String, remainingNames: List[Header]):List[String] =
    remainingNames match {
      case body :: Nil => List[String]()
      case  x :: xs =>
        val rawBodyMessage = org.jsoup.Jsoup.parse(bodyMessage).text()
        val nextHeader:Either[String,Header] =findNextHeader(xs,rawBodyMessage)

        if(rawBodyMessage.indexOf(x.name).!=(-1) && nextHeader.isRight){
          val chunkedHtmlText = bodyMessage.slice(bodyMessage.indexOf(x.name.trim)+x.name.trim.length,bodyMessage.indexOf(nextHeader.getOrElse(Header("NotFound")).name.trim))
          hrefLinkList = (hrefLinkList.:::(org.jsoup.Jsoup.parse(chunkedHtmlText).select("a").eachAttr("href").asScala.toList)).reverse
          println("hrefLinkList :"+org.jsoup.Jsoup.parse(chunkedHtmlText).select("a").eachAttr("href").asScala.toList+" for headers from "+x.name+" to "+nextHeader.getOrElse(Header("NotFound")).name.trim)
          headerContentFilter(bodyMessage,org.jsoup.Jsoup.parse(chunkedHtmlText).text(),x.name) :: headerNamesIterator(bodyMessage,xs)
        }else
          List[String]()
      case Nil => List[String]()
    }

  private[this] def headerContentFilter(htmlheaderContents: String, headerContents: String, headerName: String): String = {
    headerContents.split("\\n").filter(headerContent => headerContent.trim.length != 0).foreach({ headerContent =>
      var lastIndex = headerContent //Get the un-HTML-ed code from headerContent
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

      val preInvestmentRoundKeywords :List[String] =List("in pre-Series","in Series","in seed","from")
      val indexFound2 = calculateMinIndex(preInvestmentRoundKeywords,lastIndex)

      val afterInvestmentRoundKeywords :List[String] =List("funding","in financing","financing","valuation")
      val indexFound3 = calculateMinIndex(afterInvestmentRoundKeywords,lastIndex)

      val prelinkKeywords :List[String] =List("has more here","has much more here","More here","here")  //".",
      val indexFound5 = calculateMinIndex(prelinkKeywords,lastIndex)

      //println("indexFound: "+indexFound+" and indexFound2: "+indexFound2+" and lastIndex.indexOf(indexFound2) "+lastIndex.indexOf(indexFound2) +" must be < indexFound3 :"+indexFound3+" which is lastIndex.indexOf(indexFound3)"+lastIndex.indexOf(indexFound3))
      //InvestedAmount
      var investmentAmount =""
      if(indexFound2 != "not_found" && compareIndexes(indexFound5,indexFound2,lastIndex) && compareIndexes(indexFound3,indexFound2,lastIndex) ){
        investmentAmount = lastIndex.slice(0+lastIndex.indexOf(preIAindexFound)+preIAindexFound.length, lastIndex.indexOf(indexFound2))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound2),lastIndex.length)
      }else if(indexFound2 == "not found"  && indexFound3 != "not found" ) {
        investmentAmount = lastIndex.slice(0, lastIndex.indexOf(indexFound3))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound2),lastIndex.length)
      } else if(indexFound2 != "not found"  && indexFound3 != "not found"  && compareIndexes(indexFound5,indexFound2,lastIndex) ) {
        investmentAmount = lastIndex.slice(0+lastIndex.indexOf(preIAindexFound)+preIAindexFound.length, lastIndex.indexOf(indexFound3))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3),lastIndex.length)
      }

      val preInvestorsKeywords :List[String] =List("led by","co-led by","from","include","led the round")  //".",
      val indexFound4 = calculateMinIndex(preInvestorsKeywords,lastIndex)

      //investmentRound
      var investmentRound =""
      if(indexFound3 != "not_found" && indexFound2 != "not_found" && compareIndexes(indexFound4,indexFound2,lastIndex) ){
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
        csvWriter.writeRow(List(name,age,based,valueProposition,investmentAmount,investmentRound,investors,link,hrefLinkList.headOption.getOrElse(""),emailDate))
        hrefLinkList = hrefLinkList.drop(1)
      }else{
        csvWriter.writeRow(List(name,age,based,valueProposition,investmentAmount,investmentRound,investors,"",emailDate))
      }

    })
    ""
  }

}