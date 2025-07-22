Compile / unmanagedSourceDirectories ++= {
  val root = baseDirectory.value.getParentFile

  Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native",
    "test-interface-common",
    "test-runner"
  ).flatMap { dir =>
    Seq(
      root / s"$dir/src/main/scala",
      root / s"$dir/jvm/src/main/scala"
    )
  }
}

addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.1")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.4")

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "5.13.3.202401111512-r"
libraryDependencies += "me.bechberger" % "ap-loader-all" % "2.9-8"

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
