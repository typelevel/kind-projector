name := "kind-projector"

version := "0.3"

scalaVersion := "2.9.2"

libraryDependencies <++= (scalaVersion) {
  v => Seq("org.scala-lang" % "scala-compiler" % v)
}

scalacOptions in console in Compile <+= (packageBin in Compile) map {
  pluginJar => "-Xplugin:" + pluginJar
}

scalacOptions in Test <+= (packageBin in Compile) map {
  pluginJar => "-Xplugin:" + pluginJar
}
