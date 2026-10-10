ThisBuild / scalaVersion := sys.props("scala.version")

lazy val lib = project.enablePlugins(ScalaNativePlugin)
lazy val app = project
  .enablePlugins(ScalaNativePlugin)
  .dependsOn(lib)
  .settings(
    checkGenerated := {
      val classes = (Compile / classDirectory).value
      val files = (classes / "shared" ** "Greeting*.nir").get()
      assert(files.exists(_.getName.contains("$$Lambda$")))
      assert(files.exists(_.getName.contains("ReflectivelyInstantiate")))
    },
    checkRecompiled := {
      val classes = (Compile / classDirectory).value
      val names =
        (classes / "shared" ** "Greeting*.nir").get().map(_.getName).toSet
      assert(names == Set("Greeting.nir", "Greeting$.nir"), names)
    },
    checkRemoved := {
      val classes = (Compile / classDirectory).value
      assert((classes / "shared" ** "Greeting*.nir").get().isEmpty)
    }
  )

lazy val checkGenerated =
  taskKey[Unit]("Check Native-only products are generated")
lazy val checkRecompiled =
  taskKey[Unit]("Check stale products of a changed source")
lazy val checkRemoved = taskKey[Unit]("Check deleted source NIR products")
