<<<<<<< Updated upstream
package com.example

=======
package com.ScalaScraper
import scala.io.Source
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
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
=======
/*     val code :String  = """Massive Fundings
    Faire, a nearly three-year-old, San Francisco-based curated wholesale marketplace that connects independent retailers and makers, has raised $150 million in Series D funding at a $1 billion valuation. Lightspeed Venture Partners and Founders Fund co-led the round, joined by including earlier backers Forerunner Ventures, YC Continuity, and Khosla Ventures. Crunchbase News has more here.
    Big-But-Not-Crazy-Big Fundings 
    Brut, a 3.5-year-old, Paris-based video news startup that's focused on social good and social impact, has raised $40 million in Series B funding, money it will use, in part, to launch in the U.S. The round was led by Red River West and blisce. Other investors include Aryeh Bourkoff, the founder and CEO of LionTree; and Eric Zinterhofer, a founding partner of Searchlight Capital Partners. TechCrunch has more here.
    Datameer, a 10-year-old, San Francisco-based big data analytics platform, has raised $40 million in funding led by ST Telemedia, with participation from earlier backers Redpoint Ventures, Kleiner Perkins, Nextworld Capital, Citi Ventures, and Top Tier Capital Partners. TechCrunch has more here. 
    Duality, a three-year-old, Newark, N.J.-based company that builds software based on homomorphic encryption — a technique that encrypts an organization’s data in a way that lets it stay encrypted even as the company collaborates with third parties that also process the data — has raised $16 million in funding. The Series A round is being led by Intel Capital, with participation from Hearst Ventures and Team8. TechCrunch has more here.
    ElasticRun, a three-year-old Pune, India-based startup that's building a logistics network to supply goods to the thousands of neighborhood stores that dot large and small cities, towns, and villages in India, has $40 million in a Series C funding led by Prosus Ventures, the newly public company spun out of Naspers. Earlier investors Avataar Ventures and Kalaari Capital also participated in the round, which brings ElasticRun's total funding to $55.5 million. TechCrunch explains the opportunity it's chasing here.
    Mirror, a three-year-old, New York-based fitness content streaming device that users can hang and use like a traditional full-length mirror, has raised $34 million more in a Series B-1 funding round led by Point72 Ventures. , whose founder, hedge fund billionaire Steve Cohen, is also joining the board. The company has now raised $72 million to date. More here.
    Muy, a two-year-old, Bogota, Colombia-based dark kitchen company that's producing its own food, has raised $15 million in Series B funding to expand into Mexico and, later, Brazil. The Mexico-based investor ALLVP led the round, with participation from earlier backer Seaya. The company has now raised $20.5 million altogether. TechCrunch has more here.
    Particle, a seven-year-old, San Francisco-based platform for Internet of Things devices, has raised $40 million in its latest round of funding. Qualcomm Ventures and Energy Impact Partners led the round, with participation from earlier investors Root Ventures, Bonfire Ventures, Industry Ventures, Spark Capital, Green D Ventures, Counterpart Ventures, and SOSV. The company has now raised $81 million to date. TechCrunch has more here. 
    Rokt, a seven-year-old, New York-based  e-commerce marketing technology, today announced an investment of $48 million from global investment firm, TDM Growth Partners, as well as existing investors. More here. 
    Stampli, a four-year-old, Mountain View, Ca.-based company looking to automate invoice management, has today announced the close of a $25 million Series B round. The funding was led by SignalFire, with participation from Hillsven Capital, Bloomberg Beta, as well as new investor NextWorld Capital. TechCrunch has more here.
    Smaller Fundings 
    Artmyn, a three-year-old, Saint-Sulpice, Switzerland-based startup focused on the highly accurate digitization of visual artworks (it has developed a portable scanner that captures gigabytes of data describing the artwork in its finest details that it says results in true-to-life a visualization), has raised $4 million in funding. The round was co-led by the online marketplace for fine art and antiques Invaluable, along with earlier backers. More here.
    CoinList, a two-year-old, San Francisco-based company that connects investors to crypto projects, has raised $10 million in funding led by Polychain Capital, with participation from Jack Dorsey, among others. The WSJ has more here.
    Link3D, a three-year-old, New York-based startup behind an enterprise additive manufacturing execution system that connects organizations' digital manufacturing ecosystems, has raised $7 million in funding led by the AI-focused venture capital firm AI Capital. More here.  
    Ruti, a nearly 11-year-old, Belmont, Ca.-based fashion brand with what it describes as a high-tech, highly personalized in-store experience (it has eight stores so far), has raised $6 million in a Series A funding led by Viola Ventures. More here.
    Stardog, a 12-year-old, Arlington, Va.-based reusable, scalable knowledge graph platform that helps enterprises unify all their data, has raised $9 million in Series B funding led by Tenfore Holdings, with participation from Grotech Ventures, Boulder Ventures, and Core Capital. More here. 
    Workiz, a four-year-old, San Diego, Ca.-based startup whose software helps field service professionals manage their work, has raised $5 million in Series A funding led by Magenta Venture Partners, with participation from earlier investor Aleph. TechCrunch has more here.
    Jungle Ventures founded seven years ago in Singapore, has raised $240 million for its third Southeast Asian fund, with almost 60 percent of the capital coming from outside Asia, says Bloomberg. Investors in the fund included German development finance institution DEG, the World Bank‘s International Finance Corp., Bangkok Bank’s corporate venture capital arm, Cisco Investments and Singapore’s state investment firm, Temasek Holdings. More here.
    New Funds 
    """ */
    val filename = "/home/stkat/Desktop/Mwh.html"
    val fileContents = Source.fromFile(filename).getLines.mkString
    println(fileContents)
    //println(bodyMessageFilteringToCSVRow(code))
>>>>>>> Stashed changes
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