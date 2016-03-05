

lazy val common = Seq(
  organization := "org.scala-native",
  version := "0.1-SNAPSHOT",
  libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.2.1",
  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  scalaVersion := "2.11.7",
  // Can I have some exhaustivity checking? Pretty please?
  scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "1000000")
)

lazy val withPluginCommon = common ++ Seq(
  scalacOptions ++= Seq(
    "-Xplugin:plugin/target/scala-2.11/plugin_2.11-0.1-SNAPSHOT.jar"
  ),
  libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.7"
)

lazy val nir =
  project.in(file("nir")).
    settings(common: _*).
    dependsOn(util)

lazy val util =
  project.in(file("util")).
    settings(common: _*)

lazy val plugin =
  project.in(file("plugin")).
    settings(common: _*).
    settings(
      unmanagedSourceDirectories in Compile ++= Seq(
        (scalaSource in (nir, Compile)).value,
        (scalaSource in (util, Compile)).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    ).
    dependsOn(nir)

lazy val compiler =
  project.in(file("compiler")).
    settings(common: _*).
    settings(
      libraryDependencies += "commons-io" % "commons-io" % "2.4"
    ).
    dependsOn(nir)

lazy val javalib =
  project.in(file("javalib")).
    settings(withPluginCommon: _*).
    dependsOn(plugin)

lazy val nativelib =
  project.in(file("nativelib")).
    settings(withPluginCommon: _*).
    dependsOn(plugin)

lazy val sandbox =
  project.in(file("sandbox")).
    settings(withPluginCommon: _*).
    settings(
      scalacOptions += "-Xprint:all"
    ).
    dependsOn(plugin)


