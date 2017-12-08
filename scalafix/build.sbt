// Necessary because scalafix is also the name of the key, and its in scope.
import _root_.scalafix.Versions.{ scala212, version => scalafixVersion }

// Use a scala version supported by scalafix.
scalaVersion in ThisBuild := scala212
scalacOptions in ThisBuild ++= List("-encoding", "utf8")
scalacOptions in ThisBuild ++= List("-deprecation", "-feature", "-unchecked", "-Xlint")
scalacOptions in ThisBuild ++= List(
  "-language:higherKinds",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

val rules = project settings (
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % scalafixVersion
)

val input = project settings (
  scalafixSourceroot := (sourceDirectory in Compile).value
)

val output = project settings (
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
  // TODO: Switch to dependsOn RootProject(file("..")) % "plugin->default(compile)"
)

val tests = project dependsOn (input, rules) enablePlugins BuildInfoPlugin settings (
  libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % scalafixVersion % Test cross CrossVersion.full,
  buildInfoPackage := "fix",
  buildInfoKeys := Seq[BuildInfoKey](
        "inputSourceroot" -> (sourceDirectory in  input in Compile).value,
       "outputSourceroot" -> (sourceDirectory in output in Compile).value,
    "inputClassdirectory" -> ( classDirectory in  input in Compile).value
  )
)
