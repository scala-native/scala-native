unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile

  Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native"
  ).map(dir => root / s"$dir/src/main/scala")
}

libraryDependencies ++= Seq(
  "org.scala-sbt"    % "scripted-plugin"      % sbtVersion.value,
  "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "3.2.0.201312181205-r",
  "com.lihaoyi"      %% "fastparse"           % "0.4.2",
  "com.lihaoyi"      %% "scalaparse"          % "0.4.2",
  compilerPlugin(
    "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
)

addSbtPlugin("org.scala-native" % "sbt-cross"       % "0.1.0-SNAPSHOT")
addSbtPlugin("com.eed3si9n"     % "sbt-dirty-money" % "0.1.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding",
  "utf8"
)

libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.1"

libraryDependencies += compilerPlugin(
  "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
