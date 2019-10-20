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
                      "<insert_email_here>",
                      "<insert_password_here>")
        val inbox = store.getFolder("Inbox")
        inbox.open(Folder.READ_ONLY)

        val messages: Array[Message] = inbox.getMessages()
        var count: Int = 0
        val georgeAddr: InternetAddress = new InternetAddress(
          "<retrievable_email>")
        val typer = new TypeClass()
        //println(typer.manOf(georgeAddr))

        for (message <- messages) {
          //message.getReplyTo.foreach(println)
          //println(message.getReplyTo)
          val emailStr: String =
            listIterator(message.getReplyTo.toList, georgeAddr)
          val resultMulti = if (!emailStr.equals(" ") && message.isMimeType("multipart/*")) {
            val resultMulti = getTextFromMimeMultipart(message.getContent.asInstanceOf[MimeMultipart])
            println("resultMulti =" + resultMulti)
          } else if (!emailStr.equals(" ")) message.getContent.toString

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

  private[this] def listIterator(list: List[Address],
                                   georgeAddr: Address): String = list match {
      case List()                                     => ""
      case (head :: Nil) if (head.equals(georgeAddr)) => georgeAddr.toString
      case head :: rest                               => listIterator(rest, georgeAddr)
    }

  private[this] def getTextFromMimeMultipart(mimeMultipart: MimeMultipart):String = {
    val count:Int = mimeMultipart.getCount()
    val result2 =yieldContentResult("",mimeMultipart).apply(count-1)
    println(result2)
    result2
  }

  private[this] def yieldContentResult(result:String,mimeMultipart:MimeMultipart):(Int => String) = {
    case x if(x>= 0) =>
      val bodypart = mimeMultipart.getBodyPart(x)
      if(bodypart.isMimeType("text/plain")){
        //println(result + "\n" + bodypart.getContent())
        yieldContentResult(result + "\n" + bodypart.getContent(),mimeMultipart)(x-1)
      }else if(bodypart.isMimeType("text/html")){
        val html = bodypart.getContent.asInstanceOf[String]
        //println(result + "\n" + org.jsoup.Jsoup.parse(html).text())
        yieldContentResult(result + "\n" + org.jsoup.Jsoup.parse(html).text(),mimeMultipart)(x-1)
      }
      else if (bodypart.getContent.isInstanceOf[MimeMultipart]){
        //println("Ok :"+result)
        yieldContentResult(result,bodypart.getContent.asInstanceOf[MimeMultipart])(x-1)
      }
      result
    //println("Ok2 :"+result)
    //yieldContentResult(result,mimeMultipart)(x-1)
    case _ => result
  }
}