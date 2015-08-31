
lazy val common = Seq(
  libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.2.1",
  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  scalaVersion := "2.11.7",
  // Can I have some exhaustivity checking? Pretty please?
  scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "1000000")
)

lazy val ir =
  project.in(file("ir")).
    settings(common: _*).
    dependsOn(util)

lazy val util =
  project.in(file("util")).
    settings(common: _*)

lazy val compiler =
  project.in(file("compiler")).
    settings(common: _*).
    settings(
      unmanagedSourceDirectories in Compile ++= Seq(
        (scalaSource in (ir, Compile)).value,
        (scalaSource in (util, Compile)).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    ).
    dependsOn(ir)

lazy val linker =
  project.in(file("linker")).
    settings(common: _*).
    settings(
      fork in console := true
    ).
    dependsOn(ir)

lazy val sandbox =
  project.in(file("sandbox")).
    settings(common: _*).
    settings(
      scalacOptions ++= Seq(
        "-Xplugin:compiler/target/scala-2.11/compiler_2.11-0.1-SNAPSHOT.jar"
      )
    ).
    dependsOn(ir, compiler)
