inThisBuild {
  Seq(
    resolvers in Global += "scala-integration" at "https://scala-ci.typesafe.com/artifactory/scala-integration/",
    githubWorkflowPublishTargetBranches := Seq(),
    crossScalaVersions := Seq(
      "2.11.12",
      "2.12.8",
      "2.12.9",
      "2.12.10",
      "2.12.11",
      "2.12.12",
      "2.12.13",
      "2.12.14",
      "2.12.15",
      "2.12.16",
      "2.12.17",
      "2.12.18",
      "2.12.19",
      "2.12.20",
      "2.13.0",
      "2.13.1",
      "2.13.2",
      "2.13.3",
      "2.13.4",
      "2.13.5",
      "2.13.6",
      "2.13.7",
      "2.13.8",
      "2.13.9",
      "2.13.10",
      "2.13.11",
      "2.13.12",
      "2.13.13",
      "2.13.14",
      "2.13.15",
      "2.13.16",
      "2.13.17",
    ),
    organization := "org.typelevel",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("http://github.com/typelevel/kind-projector")),
    Test / publishArtifact := false,
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

lazy val `kind-projector` = project
  .in(file("."))
  .settings(
    name := "kind-projector",
    crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
    crossVersion := CrossVersion.full,
    crossScalaVersions := (ThisBuild / crossScalaVersions).value,
    publishMavenStyle := true,
    sonatypeProfileName := organization.value,
    publishTo := sonatypePublishToBundle.value,
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    Compile / unmanagedSourceDirectories ++= {
      (Compile / unmanagedSourceDirectories).value.flatMap { dir =>
        val sv = scalaVersion.value
        val suffices =
          (if (hasNewParser(sv)) "-newParser" else "-oldParser") ::
          (if (hasNewReporting(sv)) "-newReporting" else "-oldReporting") ::
          Nil
        suffices.map(suffix => file(dir.getPath + suffix))
      }
    },
    libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value,
    scalacOptions ++= Seq(
      "-Xlint",
      "-feature",
      "-language:higherKinds",
      "-deprecation",
      "-unchecked",
    ),
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
