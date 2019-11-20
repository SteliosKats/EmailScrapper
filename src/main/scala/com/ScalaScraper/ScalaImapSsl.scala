package scalamail

import java.util.{Date, Properties}

import com.github.tototoshi.csv._
import javax.mail._
import javax.mail.internet._
import org.jsoup.nodes.Element
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
  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"))  //Not-Crazy-Big Fundings
  private[this] final val excludeHeaders: List[Header] = List(Header("Exits"),Header("IPOs"),Header("People"),Header("Sponsored By"),Header("Jobs"),Header("Essential Reads"))
  private[this] final val htlmlUncheckedNames: Header = Header("Big-But-Not-<em>Crazy</em>-Big Fundings")

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
             //val startingHeaderPlusLength2 = rawBodyMessage.indexOf(htlmlUncheckedNames.name)+htlmlUncheckedNames.name.length
             val chunkedText = rawBodyMessage.substring(startingHeaderPlusLength,rawBodyMessage.indexOf(nextHeaderResult))

             val chunkedHtmlText = if(nextHeaderResult.equals("Big-But-Not-Crazy-Big Fundings") && bodyMessage.indexOf(nextHeaderResult) == -1)
               bodyMessage.substring(startingHeaderPlusLength,bodyMessage.indexOf(htlmlUncheckedNames.name))
             else
               bodyMessage.substring(startingHeaderPlusLength,bodyMessage.indexOf(nextHeaderResult))  //
             
             textLinkList = org.jsoup.Jsoup.parse(chunkedHtmlText).select("a").asScala.toList
             .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
             .filter{case (text,href) => text.contains("here")}.reverse
             headerContentFilter(chunkedText) :: headerNamesIterator(bodyMessage.substring(startingHeaderPlusLength,bodyMessage.length),xs)
          }else if(rawBodyMessage.indexOf(x.name.trim).!=(-1) && (xs == Nil || nextHeader.isLeft)){
             textLinkList = org.jsoup.Jsoup.parse(bodyMessage).select("a").asScala.toList
             .map(x => Tuple2(x.asInstanceOf[Element].html().toLowerCase,x.asInstanceOf[Element].attr("href")))
             .filter{case (text,href) => text.contains("here")}.reverse
             val processedBody = rawBodyMessage.substring(rawBodyMessage.indexOf(x.name.trim),rawBodyMessage.indexOf(nextUncheckedHeader.fold(l => "NotFound", r => r.name.trim)))
            headerContentFilter(processedBody)
            Nil
          } else {
            headerNamesIterator(bodyMessage, xs)
          }
      case Nil => Nil
    }

    private[this] def headerContentFilter(headerContents: String): String  = {
      headerContents.split("\\n").filter(headerContents => headerContents.trim.length != 0).foreach({ headerContent =>
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
  
        val prelinkKeywords :List[String] =List("More here and here","more here and here","has more here","has much more here","More here","here")  //".",
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
              headerContentFilter(lastIndex.substring(lastIndex.indexOf(link)+link.length +1,lastIndex.length))
            }
          }else {
            link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.length)
            lastIndex = lastIndex.slice(0,lastIndex.indexOf(".")).appended("\n").slice(0,lastIndex.length).toString
          }
        }else{
          link ="@not_found"
          if(lastIndex.trim.length != 0){
            headerContentFilter(lastIndex)
          }
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