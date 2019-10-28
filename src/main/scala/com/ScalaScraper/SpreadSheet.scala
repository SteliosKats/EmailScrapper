package com.example


object Spreadsheets {


  trait Email
  case class Header(name :String) extends Email
  case class SubHeader(name :String) extends Email
  case class Contents(body :Body) extends Email
  case class Body(name :String, age:String,based:String,value_proposition :String,investment_amount:String,
                  investment_round:String,lead_VCs:String,rest_VCs:String,link:String) extends Email

  private[this] final val headerNames: List[Header] = List(Header("Massive Fundings "),Header("Big-But-Not-Crazy-Big Fundings ")
    ,Header("Smaller Fundings "),Header("Not-Saying-How-Much Fundings "),Header("New Funds"))

  def main(args: Array[String]) {
    val code :String  = """Massive Fundings
     Menlo Security, a six-year-old, Palo Alto, Ca.-based startup that says it isolates and executes all web content in the cloud, so users can safely interact with websites, links and documents online without compromising their security, has closed on $75 million in Series D funding. JP Morgan Asset Management led the round (using its clients' money), with earlier investors also jumping in, including General Catalyst, Sutter Hill Ventures, Osage University Partners, American Express Ventures, HSBC, JP Morgan Chase and Engineering Capital. More here.
     Tsign, a 17-year-old, Hangzhou, China-based e-signature service company, has raised nearly  $100 million in Series C funding led by Ant Financial. China Money Network has more here (though a subscription is required).
     Big-But-Not-Crazy-Big Fundings
     Mogrify, a four-year-old, U.K.-based cell therapy startup focused on arthritis, has raised $16 million in Series A funding led by Ahren, with participation from Parkwalk, 24Haymarket, and the University of Bristol Enterprise Fund. The Times has more here.
     Radius Networks, an eight-year-old, Washington, D.C.-based technology company that uses its machine learning location platform to help businesses conduct location-based transactions with their customers, has raised $15 million in Series A funding, Backers include new and earlier investors, including Contour Venture Partnersand Core Capital Partners. CityBizList has more here.
     Smaller Fundings
     Redesign Science, a platform technology company hoping to advance new small molecule therapeutics, has raised $2 million in seed funding led by Notation, with participation from Morningside Venture Capital, Third Kind Venture Capital, Refactor Capital, and angel investor Thomas Weingarten. More here.
     Latent AI, an 11-month-old,  Menlo Park, Ca.- based AI processing company that spun out of SRI International late last year, has raised $3.5 million in seed funding led by Steve Jurvetson's Future Ventures. More here.
     Not-Saying-How-Much Fundings
     VIPKid, the six-year-old, Beijing, China-based online education platform, says it has closed on an undisclosed amount of Series E funding led by Tencent Holdings. According to previous media reports, VIPKid was looking to raise as much as $500 million in the round at a valuation of $4.5 billion, up from $3.5 billion valuation it was assigned by investors last year. DealStreetAsia has more here.
     New Funds
     Three self-professed gamers and veteran entrepreneurs have come together to form a brand new VC firm called Hiro Capital that's focused on games, e-sports, and digital sports in Europe and the UK. It's looking to raise up to €100 million, it will be based in London and Luxembourg, and the founding team consists of Ian Livingstone CBE, co-founder of Games Workshop; Luke Alvarez, co-founder of Inspired Entertainment; and Cherry Freeman, co-founder of LoveCrafts. Tech.eu has the story here."""

    println(bodyMessageFilteringToCSVRow(code))
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
      if(lastIndex.contains("year-old")){
        val age = lastIndex.slice(3, lastIndex.indexOf(","))
        lastIndex = lastIndex.slice(lastIndex.indexOf(",")+1,lastIndex.size)
        println("Age:"+age)
      }

      val investmentKeywords :List[String] =List("has raised","just raised","raised","has closed on","has closed","closed")

      //println("has :"+ lastIndex.indexOf("has")+"  raised :"+lastIndex.indexOf("raised")+" has+raised :"+lastIndex.indexOf("has")+lastIndex.indexOf("raised"))

      val array1:List[Int] = investmentKeywords.map(word => word.split("\\W").head.size)
      val functionalResult = investmentKeywords.map(word => word.split("\\W") match {
        case x if x.size >=2 =>  Array(x.head,x.last).foldLeft("0".toInt)((acc,wrd) => lastIndex.indexOf(wrd)-acc) -(x.size -1)  // -word.split("\\W").size
        case z if z.size < 2 =>  lastIndex.indexOf(z)
      })

      //println("Functional result :"+investmentKeywords.map(word =>lastIndex.indexOf(word)))
      //println(investmentKeywords.zipWithIndex)
      //println(investmentKeywords.zipWithIndex.min._2)
      //println(lastIndex.indexOf("has closed in"))
      //val minIndexOfKeyword:Option[Int] =(array1.zip(functionalResult)).map{case (x,y) => if(x.equals(y)) x}.zipWithIndex.filter{case (x,y) => !x.toString.equals("()")}.map{case (x,y) => y}.minByOption(empty => empty)

      if(lastIndex.contains("-based")){
        val based = lastIndex.slice(1, lastIndex.indexOf("-based")+7)
        lastIndex = lastIndex.slice(lastIndex.indexOf("-based")+7,lastIndex.size)
        println("Based:"+based)
        val result = investmentKeywords.filter(value => lastIndex.indexOf(value)!= -1).minByOption(empty => empty).getOrElse("not_found")
        println(investmentKeywords.filter(value => lastIndex.indexOf(value)!= -1).map(value => lastIndex.indexOf(value)) )
        if(!result.equals("not_found")){
          println("result:"+result)
          val valueProposition = lastIndex.slice(0, lastIndex.indexOf(result)-1)
          lastIndex = lastIndex.slice(lastIndex.indexOf(result)+1,lastIndex.size)
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

      if(result2 != "not_found"){
        val investmentAmount = lastIndex.slice(0+lastIndex.indexOf(result2)+result2.size, lastIndex.indexOf(result2))
        lastIndex = lastIndex.slice(lastIndex.indexOf(result2),lastIndex.size)
        println("investmentAmount:"+investmentAmount)
        val investmentRound = lastIndex.slice(3, lastIndex.indexOf("funding"))
        lastIndex = lastIndex.slice(lastIndex.indexOf("funding")+7,lastIndex.size)
        println("investmentRound:"+investmentRound)
      }
    })
    ""
  }

}