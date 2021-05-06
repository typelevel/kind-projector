import ReleaseTransformations._

inThisBuild {
  Seq(
    resolvers in Global += "scala-integration" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
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
      "2.13.4",
      "2.13.5",
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

val HasScalaVersion = {
  object Matcher {
    def unapply(versionString: String) =
      versionString.takeWhile(ch => ch != '-').split('.').toList.map(str => scala.util.Try(str.toInt).toOption) match {
        case List(Some(epoch), Some(major), Some(minor)) => Some((epoch, major, minor))
        case _ => None
      }
  }
  Matcher
}

def hasNewReporting(versionString: String) = versionString match {
  case HasScalaVersion(2, 12, minor) => minor >= 13
  case HasScalaVersion(2, 13, minor) => minor >= 2
  case _ => false
}

def hasNewParser(versionString: String) = versionString match {
  case HasScalaVersion(2, 12, minor) => minor >= 13
  case HasScalaVersion(2, 13, minor) => minor >= 1
  case _ => false
}

def hasNewPlugin(versionString: String) = versionString match {
  case HasScalaVersion(2, 10, _) => false
  case _ => true
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
        val sv = scalaVersion.value
        val suffices =
          (if (hasNewParser(sv)) "-newParser" else "-oldParser") ::
          (if (hasNewReporting(sv)) "-newReporting" else "-oldReporting") ::
          (if (hasNewPlugin(sv)) "-newPlugin" else "-oldPlugin") ::
          Nil
        suffices.map(suffix => file(dir.getPath + suffix))
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
    Test / scalacOptions ++= (scalaVersion.value match {
      case HasScalaVersion(2, 13, n) if n >= 2 => List("-Wconf:src=WarningSuppression.scala:error")
      case _                                   => Nil
    }) ++ List("-P:kind-projector:underscore-placeholders"),
    console / initialCommands := "import d_m._",
    Compile / console / scalacOptions := Seq("-language:_", "-Xplugin:" + (Compile / packageBin).value),
    Test / console / scalacOptions := (Compile / console / scalacOptions).value,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    Test / fork := true,
    //Test / scalacOptions ++= Seq("-Xprint:typer", "-Xprint-pos"), // Useful for debugging
  )
