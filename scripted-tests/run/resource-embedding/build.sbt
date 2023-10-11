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
      _.withEmbedResources(false)
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

// Embedded in a directory
lazy val projectD = (project in file("D"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
    },
    scalaVersion := commonScalaVersion
  )

// Binary files with bytes 0x00 and 0xFF
lazy val projectE = (project in file("E"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
    },
    scalaVersion := commonScalaVersion
  )

lazy val projectF = (project in file("F"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
        .withResourceIncludePatterns(Seq("**.txt", "dir/**"))
    },
    scalaVersion := commonScalaVersion
  )

lazy val projectG = (project in file("G"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
        .withResourceIncludePatterns(Seq("**"))
        .withResourceExcludePatterns(Seq("exclude/**"))
    },
    scalaVersion := commonScalaVersion
  )

// Test scala-native doesn't embed resources like "/LICENSE" and "/NOTICE" if they are not explicitly included.
lazy val projectH = (project in file("H"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    nativeConfig ~= {
      _.withEmbedResources(true)
        .withResourceIncludePatterns(Seq("*.conf"))
        .withResourceExcludePatterns(Seq("exclude/**"))
    },
    scalaVersion := commonScalaVersion
  )
