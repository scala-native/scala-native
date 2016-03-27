unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile
  Seq(
    root / "util/src/main/scala",
    root / "nir/src/main/scala",
    root / "tools/src/main/scala",
    root / "sbtplugin/src/main/scala"
  )
}

libraryDependencies += "commons-io" % "commons-io" % "2.4"
