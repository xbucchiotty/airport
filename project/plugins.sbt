//resolvers += Resolver.url(
//  "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/",
//   new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
// )(Resolver.ivyStylePatterns)

//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
