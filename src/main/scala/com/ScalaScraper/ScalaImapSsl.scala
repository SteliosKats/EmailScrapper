package scalamail

import java.util.Properties

import javax.mail._
import javax.mail.internet._

trait Email
case class Header(name :String) extends Email
case class SubHeader(name :String) extends Email
case class Contents(body :String) extends Email

class TypeClass {
  def manOf[T: Manifest](t: T): Manifest[T] = manifest[T]
}

object ScalaImapSsl {

  def main(args: Array[String]) {
      val props: Properties = System.getProperties()
      props.setProperty("mail.store.protocol", "imaps")
      val session: Session = Session.getDefaultInstance(props, null)
      val store = session.getStore("imaps")
      try {
        // use imap.gmail.com for gmail
        store.connect("imap.gmail.com",
                      "stelios.katsiadramis@gmail.com",
                      "vdbxbdywtswnocon")
        val inbox = store.getFolder("Inbox")
        inbox.open(Folder.READ_ONLY)

        val messages: Array[Message] = inbox.getMessages()
        var count: Int = 0
        val georgeAddr: InternetAddress = new InternetAddress(
          "George Karabelas <gk@venturefriends.vc>")
        var counter =0
        for (message:Message <- messages) {
          counter +=1
          val emailStr: String =
            listIterator(message.getReplyTo.toList, georgeAddr)
          val resultMulti:String =
            if (!emailStr.equals("") && message.isMimeType("multipart/*")){
              val result = getTextFromMimeMultipart(message.getContent.asInstanceOf[MimeMultipart],counter)
              if(!result.indexOf("Massive Fundings ").equals(-1) && !result.indexOf("Big-But-Not-Crazy-Big Fundings ").equals(-1)) {
                println(
      result.substring(result.indexOf("Massive Fundings "),
                       result.indexOf("Big-But-Not-Crazy-Big Fundings ") - 1))
              }
              ""
            }else message.getContent.toString


            //println(resultMulti.toString)
          //message.getReplyTo().foreach(println) //get Sender of Email
          //message.getAllRecipients().foreach(println) //Get All Recipients
          //message.getReplyTo.foreach(addr => println(addr.toString))
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
}