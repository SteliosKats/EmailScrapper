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

  private[this] final val headerNames :List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings ")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"))

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
        var count: Int = 0
        val georgeAddr: InternetAddress = new InternetAddress(
          "George Karabelas <gk@venturefriends.vc>")

        val outputFile = new BufferedWriter(new FileWriter("/home/stelios/Downloads/output.csv"))
        val csvWriter = new CSVWriter(outputFile)
        csvWriter.writeAll(List(List("a", "b", "c"), List("d", "e", "f")))


        var counter =0
        for (message:Message <- messages) {
          counter +=1
          val emailStr: String =
            listIterator(message.getReplyTo.toList, georgeAddr)
          val resultMulti:String =
            if (!emailStr.equals("") && message.isMimeType("multipart/*")){
              val result = getTextFromMimeMultipart(message.getContent.asInstanceOf[MimeMultipart],counter)
              csvWriter.writeRow(bodyMessageFilteringToCSVRow(result))
              /*if(!result.indexOf("Massive Fundings ").equals(-1) && !result.indexOf("Big-But-Not-Crazy-Big Fundings ").equals(-1)) {
                println(
      result.substring(result.indexOf("Massive Fundings "),
                       result.indexOf("Big-But-Not-Crazy-Big Fundings ") - 1))
              }
              ""*/
            }else message.getContent.toString

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
    //println(result2)
  }

  private[this] def yieldContentResult(result:String,mimeMultipart:MimeMultipart):(Int => String) = {
    case x if(x>= 0) =>
      val bodypart = mimeMultipart.getBodyPart(x)
      if(bodypart.isMimeType("text/plain")){
        //println(result + "\n" + bodypart.getContent())
        yieldContentResult(result + "\n" + bodypart.getContent(),mimeMultipart)(x-1)
      }else if(bodypart.isMimeType("text/html")){
        val html = bodypart.getContent.asInstanceOf[String]
        val emailAsString = org.jsoup.Jsoup.parse(html).text()
        //println("Elements:"+emailAsString.substring(emailAsString.indexOf("Massive Fundings "),emailAsString.indexOf("Big-But-Not-Crazy-Big Fundings ")-1))
        yieldContentResult(result + "\n" + org.jsoup.Jsoup.parse(html).text(),mimeMultipart)(x-1)
      }
      else if (bodypart.getContent.isInstanceOf[MimeMultipart]){
        //println("Ok :"+result)
        yieldContentResult(result,bodypart.getContent.asInstanceOf[MimeMultipart])(x-1)
      }else
        result
    //println("Ok2 :"+result)
    //yieldContentResult(result,mimeMultipart)(x-1)
    case _ => result
  }

  private[this] def bodyMessageFilteringToCSVRow(bodyMessage :String):List[String] = {
    headerNames.foreach(header => bodyMessage.headerContentFilter(header))
    return Nil
  }

  private[this] def headerContentFilter(headerBody :String): Unit =
    headerNames match {
      case  x :: xs =>
      case body:: Nil =>  body.name.substring(body.name.indexOf(body.name.length -1),
        body.name.indexOf(body.name.length))
      case _ =>
    }
}