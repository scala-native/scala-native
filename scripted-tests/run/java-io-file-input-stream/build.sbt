import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

ScalaNativePlugin.projectSettings

scalaVersion := "2.11.8"

lazy val createFileWithAlreadyWrittenText =
  taskKey[Unit]("Creating a file with some text on it")

lazy val deleteFile = taksKey[Unit]("Deletes the test file")

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
