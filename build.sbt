

name:="SouthWind"

version := "1.0"

scalaVersion:="2.9.1"


libraryDependencies ++= Seq(
"org.squeryl" %% "squeryl" % "0.9.5-SNAPSHOT",
"mysql" % "mysql-connector-java" % "5.1.15"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _)


resolvers += "Snapshots Repository" at "http://scala-tools.org/repo-snapshots"


ivyLoggingLevel := UpdateLogging.Full