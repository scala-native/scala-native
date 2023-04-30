Compile / unmanagedSourceDirectories ++= {
  val root = baseDirectory.value.getParentFile

  Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native",
    "test-interface-common",
    "test-runner"
  ).map(dir => root / s"$dir/src/main/scala")
}

addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.1")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.0.1")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.4")

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "5.10.0.202012080955-r"

// scalacOptions used to bootstrap to sbt prompt.
// In particular, no "-Xfatal-warnings"
// A stricter set of Options is used in the project root build.sbt.
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
  "-unchecked"
)
