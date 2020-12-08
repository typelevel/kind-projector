import ReleaseTransformations._

inThisBuild {
  Seq(
    githubWorkflowPublishTargetBranches := Seq(),
    crossScalaVersions := Seq(
      "2.10.7",
      "2.11.12",
      "2.12.8",
      "2.12.9",
      "2.12.10",
      "2.12.11",
      "2.12.12",
      "2.13.0",
      "2.13.1",
      "2.13.2",
      "2.13.3",
      "2.13.4"
    ),
    organization := "org.typelevel",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("http://github.com/typelevel/kind-projector")),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := Function.const(false),
    pomExtra := (
      <scm>
        <url>git@github.com:typelevel/kind-projector.git</url>
        <connection>scm:git:git@github.com:typelevel/kind-projector.git</connection>
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
    ),
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
      pushChanges
    )
  )
}

def parseScalaVersion(versionString: String) =
  versionString.split('.').toList.map(str => scala.util.Try(str.toInt).toOption) match {
    case List(Some(epoch), Some(major), Some(minor)) => Some((epoch, major, minor))
    case _ => None
  }

lazy val `kind-projector` = project
  .in(file("."))
  .settings(
    name := "kind-projector",
    crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
    crossVersion := CrossVersion.full,
    crossScalaVersions := (ThisBuild / crossScalaVersions).value,
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
    Compile / unmanagedSourceDirectories ++= {

      (Compile / unmanagedSourceDirectories).value.flatMap { dir =>
        val maxMinor = 6
        parseScalaVersion(scalaVersion.value) match {
          case Some((2, major, minor)) if major <= 12 || minor == 0 =>
            (0 to maxMinor).map(max => file(s"${dir.getPath}-2.13.$max-"))
          case Some((2, 13, n)) =>
            (0 to n).map(min => file(s"${dir.getPath}-2.13.$min+")) ++ 
            (n to maxMinor).map(max => file(s"${dir.getPath}-2.13.$max-"))
        }
      }
    },
    libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value,
    libraryDependencies ++= (scalaBinaryVersion.value match {
      case "2.10" => List(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.1.1"
      )
      case _      => Nil
    }),
    scalacOptions ++= Seq(
      "-Xlint",
      "-feature",
      "-language:higherKinds",
      "-deprecation",
      "-unchecked",
    ),
    Compile / compile / scalacOptions += "-Xfatal-warnings",
    Test / scalacOptions ++= {
      val jar = (Compile / packageBin).value
      Seq("-Yrangepos", s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
    },
    Test / scalacOptions ++= (parseScalaVersion(scalaVersion.value) match {
      case Some((2, 13, n)) if n >= 2 => List("-Wconf:src=WarningSuppression.scala:error")
      case _                          => Nil
    }),
    Test / unmanagedSourceDirectories ++= (parseScalaVersion(scalaVersion.value) match {
      case Some((2, 13, n)) if n >= 2 =>
        (Test / unmanagedSourceDirectories).value.map(dir => file(dir.getPath ++ "-2.13.2+"))
      case _                          => Nil
    }),
    console / initialCommands := "import d_m._",
    Compile / console / scalacOptions := Seq("-language:_", "-Xplugin:" + (Compile / packageBin).value),
    Test / console / scalacOptions := (Compile / console / scalacOptions).value,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    Test / fork := true,
    //Test / scalacOptions ++= Seq("-Xprint:typer", "-Xprint-pos"), // Useful for debugging
  )
