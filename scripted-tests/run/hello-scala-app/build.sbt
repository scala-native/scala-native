enablePlugins(ScalaNativePlugin)

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

// Old versions of sbt (like 1.1.6 which is being used) don't include
// Scala version specific directiories and has problem with finding files in them
Compile / sources ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) =>
      sourceDirectory.value / "main" / "scala-2" / "HelloScalaApp.scala" :: Nil
    case _ => Nil
  }
}
