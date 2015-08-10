
lazy val shared = Seq(
  libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.2.1",
  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  scalaVersion := "2.11.7",
  // Can I have some exhaustivity checking? Pretty please?
  scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "1000000")
)

lazy val ir =
  project.in(file("ir")).settings(shared: _*).dependsOn(util)

lazy val util =
  project.in(file("util")).settings(shared: _*)

lazy val tools =
  project.in(file("tools")).
    settings(shared: _*).
    settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )).
    dependsOn(ir)
