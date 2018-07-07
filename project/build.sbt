unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile

  (root / "sbt-scala-native/src/main/scala-sbt-0.13") +:
    Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native",
    "test-interface-serialization"
  ).map(dir => root / s"$dir/src/main/scala")
}

libraryDependencies ++= Seq(
  "org.scala-sbt"    % "scripted-plugin"      % sbtVersion.value,
  "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "3.2.0.201312181205-r"
)

addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0-M2")
addSbtPlugin("com.eed3si9n"       % "sbt-dirty-money"   % "0.1.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"       % "0.5.2")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"           % "1.0.0")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin"   % "0.1.14")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"  % "0.4.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding",
  "utf8"
)
