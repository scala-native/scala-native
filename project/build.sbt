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

addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"       % "0.5.4")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"           % "2.0.0")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin"   % "0.6.1")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"     % "0.9.0")

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
