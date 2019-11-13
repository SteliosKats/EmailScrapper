package com.ScalaScraper
import javax.mail.{Folder, NoSuchProviderException, Store}

import scala.collection.JavaConverters._

object EmailUtils {
  trait Email
  case class Header(name :String) extends Email
  case class SubHeader(name :String) extends Email
  case class Contents(body :Body) extends Email
  case class Body(name :String, age:String,based:String,value_proposition :String,investment_amount:String,
                  investment_round:String,lead_VCs:String,rest_VCs:String,link:String) extends Email

  def createFolder(folderName: String,store :Store) :Boolean ={
    var isCreated = true
    try
    {
      val newFolder:Folder = store.getFolder(folderName)
      if (!newFolder.exists()) {
        isCreated = newFolder.create(Folder.HOLDS_MESSAGES)
        newFolder.setSubscribed(true)
      }
    } catch {
      case e: NoSuchProviderException =>
        e.printStackTrace()
        System.exit(1)
    }
    isCreated
  }

  def waitForMoveCompletion(folder: Folder, waitTimeout: Int): Unit = {
    folder synchronized folder.wait(waitTimeout)
  }

  def linkExtractor(doc :org.jsoup.nodes.Document): List[String] = java.util.Arrays.asList(doc.select("a[href]")).asScala.toList.map(element => element.attr("href"))

  def delimitWithDoubleQuotes(input :List[String]):List[String] = input.map{
    case x if(x.contains("\"")) => "\""+x.replaceAll("\"","\"\"")+"\""
    case x if(x.contains(",")) => "\""+input+"\""
  }
}