addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

resolvers += Resolver.url(
  "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/",
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
)(Resolver.ivyStylePatterns)

