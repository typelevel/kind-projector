name := "kind-projector"

organization := "org.spire-math"

version := "0.5.2"

scalaVersion := "2.11.4"

seq(bintrayResolverSettings: _*)

libraryDependencies <++= (scalaVersion) {
  v => Seq("org.scala-lang" % "scala-compiler" % v)
}

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked"
)

scalacOptions in console in Compile <+= (packageBin in Compile) map {
  pluginJar => "-Xplugin:" + pluginJar
}

scalacOptions in Test <+= (packageBin in Compile) map {
  pluginJar => "-Xplugin:" + pluginJar
}

crossScalaVersions := Seq("2.10.4", "2.11.4")

seq(bintrayPublishSettings: _*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
