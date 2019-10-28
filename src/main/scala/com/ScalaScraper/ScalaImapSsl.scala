package scalamail

import java.io.{BufferedWriter, FileWriter}
import java.util.Properties

import com.github.tototoshi.csv._
import javax.mail._
import javax.mail.internet._

object TypeClass {
  def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]
}

object ScalaImapSsl {

  trait Email
  case class Header(name :String) extends Email
  case class SubHeader(name :String) extends Email
  case class Contents(body :Body) extends Email
  case class Body(name :String, age:String,based:String,value_proposition :String,investment_amount:String,
                  investment_round:String,lead_VCs:String,rest_VCs:String,link:String) extends Email

  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings ")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"))

  private[this] final val outputFile = new BufferedWriter(new FileWriter("/home/stelios/Downloads/output.csv"))
  private[this] final val csvWriter = new CSVWriter(outputFile)

  private[this] final val georgeAddr: InternetAddress = new InternetAddress(
    "George Karabelas <gk@venturefriends.vc>")

  def main(args: Array[String]) {
    val props: Properties = System.getProperties()
    props.setProperty("mail.store.protocol", "imaps")
    val session: Session = Session.getDefaultInstance(props, null)
    val store = session.getStore("imaps")
    try {

      store.connect("imap.gmail.com",
        "stelios.katsiadramis@gmail.com",
        "vdbxbdywtswnocon")
      val inbox = store.getFolder("Inbox")
      inbox.open(Folder.READ_ONLY)

      val messages: Array[Message] = inbox.getMessages()

      var counter =0
      for (message:Message <- messages) {
        counter +=1
        val emailStr: String =
          listIterator(message.getReplyTo.toList, georgeAddr)
          if (!emailStr.equals("") && message.isMimeType("multipart/*")){
            val result = getTextFromMimeMultipart(message.getContent.asInstanceOf[MimeMultipart],counter)
            bodyMessageFilteringToCSVRow(result)
          }//else message.getContent.toString

        csvWriter.close()
      }
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
  }

  private[this] def listIterator(list: List[Address],georgeAddr: Address): String = list match {
    case List()                                     => ""
    case (head :: Nil) if (head.equals(georgeAddr)) => georgeAddr.toString
    case head :: rest                               => listIterator(rest, georgeAddr)
  }

  private[this] def getTextFromMimeMultipart(mimeMultipart: MimeMultipart,counter: Int):String = {
    val count:Int = mimeMultipart.getCount()
    yieldContentResult("",mimeMultipart).apply(count-1)

  }

  private[this] def yieldContentResult(result: String,mimeMultipart: MimeMultipart):(Int => String) = {
    case x if(x>= 0) =>
      val bodypart = mimeMultipart.getBodyPart(x)
      if(bodypart.isMimeType("text/plain"))
        yieldContentResult(result + "\n" + bodypart.getContent(),mimeMultipart)(x-1)
      else if(bodypart.isMimeType("text/html")){
        println(result)
        yieldContentResult(result + "\n" + org.jsoup.Jsoup.parse(bodypart.getContent.asInstanceOf[String]).text(),mimeMultipart)(x-1)
        }
      else if (bodypart.getContent.isInstanceOf[MimeMultipart])
        yieldContentResult(result,bodypart.getContent.asInstanceOf[MimeMultipart])(x-1)
      else
        result
    case _ => result
  }

  private[this] def bodyMessageFilteringToCSVRow(bodyMessage: String):Unit = headerNamesIterator(bodyMessage,headerNames)

  private[this] def headerNamesIterator(bodyMessage: String, remainingNames: List[Header]):List[String] =
    remainingNames match {

      case body :: Nil =>  headerContentFilter(bodyMessage.slice(bodyMessage.indexOf(body.name)+body.name.size,bodyMessage.length),body.name) :: List[String]()

      case  x :: xs => {
        if(bodyMessage.indexOf(x.name).!=(-1) && bodyMessage.indexOf(xs.head.name).!=(-1)){
          //println(bodyMessage.slice(bodyMessage.indexOf(x.name)+x.name.size,bodyMessage.indexOf(xs.head.name)))
          headerContentFilter(bodyMessage.slice(bodyMessage.indexOf(x.name)+x.name.size,bodyMessage.indexOf(xs.head.name)),x.name) :: headerNamesIterator(bodyMessage,xs)
        }else{
          List[String]()
        }
      }
      case Nil => List[String]()
    }

  private[this] def headerContentFilter(headerContents: String, headerName: String): String = {
    //TODO filter by \n
    headerContents.split("\\n").foreach({ headerContent =>
      var lastIndex = headerContent
      var name =""
      if (lastIndex.indexOf(",") != -1) {
        name = lastIndex.slice(0, lastIndex.indexOf(","))
        lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
        //csvWriter.writeRow()
        println("Name:"+name)
      }

      val yearKeywords :List[String] =List("year-old","months-old","days-old")
      val yearIndexFound = calculateMinIndex(yearKeywords,lastIndex)

      if(yearIndexFound != "not_found"){
        val age = lastIndex.slice(3, lastIndex.indexOf(","))
        lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
        println("Age:"+age)
      }

      val preInvestmentAmountKeywords :List[String] =List("has raised","just raised","raised","has closed on","has closed","closed")
      val indexFound = calculateMinIndex(preInvestmentAmountKeywords,lastIndex)

      if(lastIndex.contains("-based")){
        val based = lastIndex.slice(1, lastIndex.indexOf("-based")+7)
        lastIndex = lastIndex.slice(lastIndex.indexOf("-based")+7,lastIndex.size)
        println("Based:"+based)

        if(!indexFound.equals("not_found")){
          val valueProposition = lastIndex.slice(0, lastIndex.indexOf(indexFound)-1)
          lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound),lastIndex.size)
          println("valueProposition:"+valueProposition)
        }else {
          val valueProposition = lastIndex.slice(0, lastIndex.indexOf(",")-1)
          lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
          println("valueProposition:"+valueProposition)
        }
      }else if(!lastIndex.contains("-based")){
        val valueProposition = lastIndex.slice(0, lastIndex.indexOf(","))
        lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
        println("valueProposition:"+valueProposition)
      }

      val preInvestmentRoundKeywords :List[String] =List("in pre-Series","in Series","in seed")
      val indexFound2 = calculateMinIndex(preInvestmentRoundKeywords,lastIndex)

      val afterInvestmentRoundKeywords :List[String] =List("funding","in financing","financing","valuation")
      val indexFound3 = calculateMinIndex(afterInvestmentRoundKeywords,lastIndex)

      val prelinkKeywords :List[String] =List("has more here.","has much more here.","More here.","here.")  //".",
      val indexFound5 = calculateMinIndex(prelinkKeywords,lastIndex)

      //println("indexFound2: "+indexFound2+" and lastIndex.indexOf(indexFound2) "+lastIndex.indexOf(indexFound2) +" must be < indexFound3 :"+indexFound3+" which is lastIndex.indexOf(indexFound3)"+lastIndex.indexOf(indexFound3))
      //InvestedAmount
      if(indexFound2 != "not_found"  && (lastIndex.indexOf(indexFound2) < lastIndex.indexOf(indexFound5) && lastIndex.indexOf(indexFound2) < lastIndex.indexOf(indexFound3) )){
        val investmentAmount = lastIndex.slice(0+lastIndex.indexOf(indexFound)+indexFound.size, lastIndex.indexOf(indexFound2))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound2),lastIndex.size)
        println("investmentAmount:"+investmentAmount)
      }else if(indexFound2 == "not found"  && indexFound3 != "not found" ) {
        val investmentAmount = lastIndex.slice(0, lastIndex.indexOf(indexFound3))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound2),lastIndex.size)
        println("investmentAmount:"+investmentAmount)
      } else if(indexFound2 != "not found"  && indexFound3 != "not found"  && lastIndex.indexOf(indexFound3) < lastIndex.indexOf(indexFound2)) {
        val investmentAmount = lastIndex.slice(0+lastIndex.indexOf(indexFound)+indexFound.size, lastIndex.indexOf(indexFound3))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3),lastIndex.size)
        println("investmentAmount3:"+investmentAmount)
      }

      val preInvestorsKeywords :List[String] =List("led by","co-led by","from","include")  //".",
      val indexFound4 = calculateMinIndex(preInvestorsKeywords,lastIndex)


      //investmentRound
      if(indexFound3 != "not_found" && indexFound2 != "not_found" && lastIndex.indexOf(indexFound4) > lastIndex.indexOf(indexFound2)){
        val investmentRound = lastIndex.slice(0, lastIndex.indexOf(indexFound3))
        lastIndex = lastIndex.slice(lastIndex.indexOf(indexFound3)+indexFound3.size,lastIndex.size)
        println("investmentRound:"+investmentRound)
      }

      if(indexFound4 != "not_found") {
        val investors = lastIndex.slice(0+lastIndex.indexOf(indexFound4)+indexFound4.size+1, lastIndex.indexOf(".",0+lastIndex.indexOf(indexFound4)+indexFound4.size+1))
        lastIndex = lastIndex.slice(lastIndex.indexOf(".",0+lastIndex.indexOf(indexFound4)+indexFound4.size+1), lastIndex.size)
        println("investors:" + investors)
      }


      if(indexFound5 != "not found"){
        println("lastIndex :"+lastIndex)
        val newYearIndex = calculateMinIndex(yearKeywords,lastIndex)
        if(lastIndex.indexOf(newYearIndex) == -1){
          val chunkedNewline = lastIndex.slice(0, lastIndex.indexOf(newYearIndex))
          chunkedNewline.slice(0,lastIndex.lastIndexOf("."))
          val link = chunkedNewline.slice(0,lastIndex.lastIndexOf("."))
          lastIndex = lastIndex.slice(lastIndex.indexOf(link)+link.size,lastIndex.size).toString
          println("link1:" + link)
        }else {
          val link = lastIndex.slice(0, lastIndex.indexOf(indexFound5)+indexFound5.size)
          lastIndex = lastIndex.slice(0,lastIndex.indexOf(".")).appended("\n").slice(0,lastIndex.size).toString
          println("link2:" + link)
        }
        //lastIndex = lastIndex.slice(lastIndex.indexOf("funding") + 7, lastIndex.size)
      }

    })
    ""
  }


  def calculateMinIndex(keywordList :List[String],lastIndex:String):String ={
    val result = keywordList.filter(value => lastIndex.indexOf(value)!= -1).minByOption(empty => empty).getOrElse("not_found")
    val minIndexOfafterInvestmentRoundKeywords = keywordList.zipWithIndex.filter{case (value,index) => lastIndex.indexOf(value)!= -1}.foldLeft(("not_found".asInstanceOf[String],-1.asInstanceOf[Int]))((acc, value) => if(acc._1 =="not_found") (value._1,value._2) else if(acc._1 !="not_found" && (lastIndex.indexOf(value._1) < lastIndex.indexOf(acc._1))) (value._1,value._2) else (acc._1,acc._2) ).asInstanceOf[Tuple2[String,Int]]._2
    keywordList(minIndexOfafterInvestmentRoundKeywords)
  }


}