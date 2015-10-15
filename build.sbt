import ReleaseTransformations._

name := "kind-projector"
organization := "org.spire-math"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
homepage := Some(url("http://github.com/non/kind-projector"))

scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0-M3")

libraryDependencies <++= (scalaVersion) { v =>
  Seq("org.scala-lang" % "scala-compiler" % v)
}

// scalac options

scalacOptions ++= Seq(
  "-feature",
  "-language:higherKinds",
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

// release stuff

releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := Function.const(false)

publishTo <<= (version).apply { v =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("Snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("Releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>git@github.com:non/kind-projector.git</url>
    <connection>scm:git:git@github.com:non/kind-projector.git</connection>
    </scm>
    <developers>
    <developer>
    <id>d_m</id>
    <name>Erik Osheim</name>
    <url>http://github.com/non/</url>
      </developer>
    </developers>
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges)
