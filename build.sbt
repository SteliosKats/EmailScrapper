name := "GmailScraper"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.google.http-client" % "google-http-client-jackson2" % "1.19.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.19.0",
  "net.ruippeixotog" %% "scala-scraper" % "2.2.0",
  "com.google.apis" % "google-api-services-gmail" % "v1-rev72-1.23.0",
  "com.google.apis" % "google-api-services-plus" % "v1-rev210-1.19.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.6",
  "com.google.gdata" % "core" % "1.47.1",
  "com.github.scopt" %% "scopt" % "4.0.0-RC2"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.ScalaScraper",
  scalaVersion := "2.13.1",
  test in assembly := {}
)

lazy val app = (project in file("app")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("scalamail.ScalaImapSsl"),
    assemblyJarName in assembly := "GmailScrapper.jar",
    // more settings here ...
  )