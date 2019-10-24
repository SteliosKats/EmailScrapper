package scalamail

import java.io.{BufferedWriter, FileWriter}
import java.util.Properties

import com.github.tototoshi.csv._
import javax.mail._
import javax.mail.internet._

class TypeClass {
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
      else if(bodypart.isMimeType("text/html"))
        yieldContentResult(result + "\n" + org.jsoup.Jsoup.parse(bodypart.getContent.asInstanceOf[String]).text(),mimeMultipart)(x-1)
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

  private[this] def headerContentFilter(headerContent: String, headerName: String): String = {
    var lastIndex = headerContent
    var name =""
    if (lastIndex.indexOf(",") != -1) {
       name = lastIndex.slice(0, lastIndex.indexOf(","))
      lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
      //csvWriter.writeRow()
      println("Name:"+name)
    }
    if(lastIndex.contains("year-old")){
      val age = lastIndex.slice(3, lastIndex.indexOf(","))
      lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
      println("Age:"+age)
    }

    val investmentKeywords :List[String] =List("has raised","just raised","raised","has closed on","has closed","closed")

    val result = investmentKeywords.filter(value => lastIndex.contains(value)).headOption.getOrElse("not_found")

    if(lastIndex.contains("-based")){
      val based = lastIndex.slice(1, lastIndex.indexOf("-based")+7)
      lastIndex = lastIndex.slice(lastIndex.indexOf("-based")+7,lastIndex.size)
      val firstOccurence =investmentKeywords.map(x => lastIndex.indexOf(x)).headOption.getOrElse(-1)
      println("Based:"+based)
      println("First Occurence :"+investmentKeywords.filter(value => lastIndex.contains(value)))
      if(result != "not_found" && !firstOccurence.equals(-1)){
        println("result :"+result)
        val valueProposition = lastIndex.slice(0, firstOccurence-1)
        lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
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

    val investmentKeywords2 :List[String] =List("in Series","in seed","has closed on","has closed","closed","has raised")
    val result2 = investmentKeywords.filter(value => lastIndex.contains(value)).headOption.getOrElse("not_found")

    if(result != "not_found"){
      val investmentAmount = lastIndex.slice(0+lastIndex.indexOf(result)+result.size, lastIndex.indexOf("in Series"))
      lastIndex = lastIndex.slice(lastIndex.indexOf("in Series"),lastIndex.size)
      println("investmentAmount:"+investmentAmount)
      val investmentRound = lastIndex.slice(3, lastIndex.indexOf("funding"))
      lastIndex = lastIndex.slice(lastIndex.indexOf("funding")+7,lastIndex.size)
      println("investmentRound:"+investmentRound)
    }
    ""
  }
}