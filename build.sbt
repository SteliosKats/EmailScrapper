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
  "com.google.gdata" % "core" % "1.47.1"
)