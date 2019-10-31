Compile / unmanagedSourceDirectories ++= {
  val root = baseDirectory.value.getParentFile

<<<<<<< HEAD
  Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native",
    "test-interface-serialization",
    "test-runner"
  ).map(dir => root / s"$dir/src/main/scala")
=======
  (root / "sbt-scala-native/src/main/scala-sbt-0.13") +:
    Seq(
      "util",
      "nir",
      "tools",
      "sbt-scala-native",
      "test-interface-serialization",
      "test-runner"
    ).map(dir => root / s"$dir/src/main/scala")
>>>>>>> Formatting changes that could not be avoided
}

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "5.5.1.201910021850-r"
)

addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0")
addSbtPlugin("com.eed3si9n"       % "sbt-dirty-money"   % "0.2.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"       % "0.5.4")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"           % "2.0.0")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin"   % "0.6.1")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding",
  "utf8"
)
