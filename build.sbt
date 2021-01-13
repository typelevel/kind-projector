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
      "2.12.13",
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
      (Compile / unmanagedSourceDirectories).value.map { dir =>
        val sv = scalaVersion.value
        val is130 = sv == "2.13.0" // use 2.12 version for 2.13.0, reporters changed in 2.13.1
        CrossVersion.partialVersion(sv) match {
          case Some((2, n)) if n < 13 || is130 => file(dir.getPath ++ "-2.12-")
          case _                               => file(dir.getPath ++ "-2.13+")
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
    Compile / compile / scalacOptions += "Xfatal-warnings",
    Test / scalacOptions ++= {
      val jar = (Compile / packageBin).value
      Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
    },
    Test / scalacOptions += "-Yrangepos",
    console / initialCommands := "import d_m._",
    Compile / console / scalacOptions := Seq("-language:_", "-Xplugin:" + (Compile / packageBin).value),
    Test / console / scalacOptions := (Compile / console / scalacOptions).value,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    Test / fork := true,
    //Test / scalacOptions ++= Seq("-Xprint:typer", "-Xprint-pos"), // Useful for debugging
  )
