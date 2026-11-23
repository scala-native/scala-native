import java.io.{File, FileOutputStream}

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

lazy val createFileWithAlreadyWrittenText =
  taskKey[Unit]("Creating a file with some text on it")

lazy val deleteFile = taskKey[Unit]("Deletes the test file")

createFileWithAlreadyWrittenText := {
  val f: File = new File("test.txt")
  f.createNewFile()
  val fop: FileOutputStream = new FileOutputStream(f, false)
  fop.write("test".getBytes())
  fop.close()
}

deleteFile := {
  val f: File = new File("test.txt")
  if (f.exists()) {
    f.delete()
  }
}
