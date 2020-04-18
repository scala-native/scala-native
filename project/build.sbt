Compile / unmanagedSourceDirectories ++= {
  val root = baseDirectory.value.getParentFile

  val modules = Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native",
    "test-interface-serialization",
    "test-runner"
  )

  modules.map(dir => root / s"$dir/src/main/scala") ++
    modules.map(dir => root / s"$dir/src/main/scala-2.12")
}

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "5.5.1.201910021850-r"
)

addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0")
addSbtPlugin("com.eed3si9n"       % "sbt-dirty-money"   % "0.2.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"       % "0.5.4")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"           % "2.0.0")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin"   % "0.6.1")
addSbtPlugin("ch.epfl.lamp"       % "sbt-dotty"         % "0.4.1")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding",
  "utf8"
)
