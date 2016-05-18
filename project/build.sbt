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

addSbtPlugin("com.geirsson" %% "sbt-scalafmt" % "0.2.5")
