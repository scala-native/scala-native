unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile
  Seq(
    root / "util/src/main/scala",
    root / "nir/src/main/scala",
    root / "tools/src/main/scala",
    root / "sbtplugin/src/main/scala"
  )
}

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "3.2.0.201312181205-r"

libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.2"

libraryDependencies += "com.lihaoyi" %% "scalaparse" % "0.4.2"

libraryDependencies += compilerPlugin(
  "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.1.0")

libraryDependencies <+= (sbtVersion) { sv =>
  "org.scala-sbt" % "scripted-plugin" % sv
}
