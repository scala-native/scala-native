Compile / scalacOptions += "-Xmacro-settings:sbt:no-default-task-cache"

val pluginVersion = Option(System.getProperty("plugin.version"))
  .getOrElse {
    sys.error(
      """|The system property 'plugin.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  }
addSbtPlugin("org.scala-native" % "sbt-scala-native" % pluginVersion)
