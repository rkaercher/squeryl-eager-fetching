resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += {
  val typesafeRepoUrl = new java.net.URL("http://repo.typesafe.com/typesafe/releases")
  val pattern = Patterns(false, "[organisation]/[module]/[sbtversion]/[revision]/[type]s/[module](-[classifier])-[revision].[ext]")
  Resolver.url("Typesafe Repository", typesafeRepoUrl)(pattern)
}

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")