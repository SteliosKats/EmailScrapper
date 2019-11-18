package com.ScalaScraper
import com.ScalaScraper.EmailUtils.Header

object ScrapeUtils {
  /* def appendHrefLinkstoRows(hrefLinks :List[String]):Unit = hrefLinks match {
      case head::Nil =>
      case head::rest =>
     } */


  def calculateMinIndex(keywordList :List[String],lastIndex :String):String ={
    val result = keywordList.filter(value => lastIndex.indexOf(value)!= -1).minByOption(empty => empty).getOrElse("not_found")
    val wordListminIndex = keywordList.zipWithIndex.filter{case (value,index) => lastIndex.indexOf(value)!= -1}.foldLeft(("not_found".asInstanceOf[String],-1.asInstanceOf[Int]))((acc, value) => if(acc._1 =="not_found") (value._1,value._2) else if(acc._1 !="not_found" && (lastIndex.indexOf(value._1) < lastIndex.indexOf(acc._1))) (value._1,value._2) else (acc._1,acc._2) ).asInstanceOf[Tuple2[String,Int]]._2
    if(result != "not_found")
      keywordList(wordListminIndex)
    else
      result
  }

  def compareIndexes(nextindex :String, previousindex :String, lastIndex :String): Boolean ={
    if(lastIndex.indexOf(nextindex) != -1 )
      lastIndex.indexOf(nextindex) > lastIndex.indexOf(previousindex)
    else
      true
  }

  def findNextHeader(remainingHeaderNames:List[Header],bodyMessage:String):Either[String,Header] = remainingHeaderNames match {
    case x :: Nil => if(bodyMessage.indexOf(x.name.trim).!=(-1)) Right(x) else Left("not_found")

    case x :: xs => if(bodyMessage.indexOf(x.name).!=(-1)) Right(x) else findNextHeader(xs,bodyMessage)

    case _  => Left("not_found")
  }

}