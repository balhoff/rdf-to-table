enablePlugins(JavaAppPackaging)

organization := "org.renci"

name := "rdf-to-table"

version := "0.2.1"

licenses := Seq("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause"))

scalaVersion := "2.13.14"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

javaOptions += "-Xmx8G"

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

libraryDependencies ++= {
  Seq(
    "org.apache.jena" % "apache-jena-libs" % "5.0.0" exclude("org.slf4j", "slf4j-log4j12"),
    "com.github.alexarchambault" %% "case-app" % "2.0.6",
    "com.outr" %% "scribe-slf4j" % "3.15.0"
  )
}
