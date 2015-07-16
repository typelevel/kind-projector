name := "kind-projector"

organization := "org.spire-math"

version := "0.6.1"

scalaVersion := "2.11.7"

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

scalacOptions in Test ++= Seq("-Yrangepos")

// Useful for debugging:
// scalacOptions in Test ++= Seq("-Xprint:typer", "-Xprint-pos")

crossScalaVersions := Seq("2.10.5", "2.11.7", "2.12.0-M2")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
