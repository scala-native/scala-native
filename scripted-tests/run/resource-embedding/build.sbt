val commonScalaVersion = {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

// No embedded resources
lazy val projectA = (project in file("A"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
    },
    scalaVersion := commonScalaVersion
  )

// Embedded but with duplicates from A
lazy val projectB = (project in file("B"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
    },
    scalaVersion := commonScalaVersion
  )
  .dependsOn(projectA)

// Embedded without duplicates from A
lazy val projectC = (project in file("C"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
    },
    scalaVersion := commonScalaVersion
  )
  .dependsOn(projectA)
