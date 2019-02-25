name := "kind-projector"
organization := "org.spire-math"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
homepage := Some(url("http://github.com/non/kind-projector"))

scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0-M5")

libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value
libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" => scala210ExtraDeps
  case _      => Nil
})
// scalac options

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-Xlint",
  "-feature",
  "-language:higherKinds",
  "-deprecation",
  "-unchecked"
)

List(Compile, Test) flatMap { config =>
  Seq(
    initialCommands in console in config := "import d_m._",
    // Notice this is :=, not += - all the warning/lint options are simply
    // impediments in the repl.
    scalacOptions in console in config := Seq(
      "-language:_",
      "-Xplugin:" + (packageBin in Compile).value
    )
  )
}

scalacOptions in Test ++= {
  val jar = (packageBin in Compile).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}

scalacOptions in Test += "-Yrangepos"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

fork in Test := true
// libraryDependencies += "org.ensime" %% "pcplod" % "1.2.1" % Test
// javaOptions in Test ++= Seq(
//   s"""-Dpcplod.settings=${(scalacOptions in Test).value.mkString(",")}""",
//   s"""-Dpcplod.classpath=${(fullClasspath in Test).value.map(_.data).mkString(",")}"""
// )

def scala210ExtraDeps = Seq(
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  "org.scalamacros" %% "quasiquotes" % "2.1.0"
)

// Useful for debugging:
// scalacOptions in Test ++= Seq("-Xprint:typer", "-Xprint-pos")

// release stuff
import ReleaseTransformations._

releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := Function.const(false)

publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

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
    <developer>
      <id>tomasmikula</id>
      <name>Tomas Mikula</name>
      <url>http://github.com/tomasmikula/</url>
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
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges)
