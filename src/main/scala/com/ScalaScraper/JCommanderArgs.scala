package com.ScalaScraper


object JCommanderArgs {
  case class Config(email :String = null,imapsPass:String =null,outputPath :String=null)
  val parser1 = new scopt.OptionParser[Config]("java -jar GmailScrapper.jar") {
    //programName("GmailScrapper"),
    head("GmailScrapper", "1.x")
    opt[String]('e', "email").required().action((x, c) => c.copy(email = x)).validate( x => {
      if(x.contains("@")) success
      else failure("Not supported email format")
    }).text("Email to get authenticated as")
    opt[String]('p', "imapsPass").required().action((x, c) => c.copy(imapsPass = x)).text("Imaps authentication password")
    opt[String]('o', "outputPath").required().action((x, c) => c.copy(outputPath = x)).text("The path in which the \"output.csv\" file will be saved")
    // option -e, --email
    // more options here...
  }
}