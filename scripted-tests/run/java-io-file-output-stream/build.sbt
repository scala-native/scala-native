import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

ScalaNativePlugin.projectSettings

scalaVersion := "2.11.8"

lazy val createFileWithAlreadyWrittenText =
  taskKey[Unit]("Creating a file with some text on it")

lazy val deleteFile = taskKey[Unit]("Deletes the test file")

lazy val constructorFileCheck = taskKey[Unit](
  "Check that a FileOutputStream initialized with only a File overwrites when writing")

lazy val constructorFileAppendTrueCheck =
  taskKey[Unit]("Check that a FileOutputStream open in append mode appends.")

val f: File = new File("test.txt")

createFileWithAlreadyWrittenText := {
  f.createNewFile()
  val fop: FileOutputStream = new FileOutputStream(f, false)
  fop.write("test".getBytes())
  fop.close()
}

constructorFileCheck := {
  val fip: FileInputStream = new FileInputStream(f)
  val size                 = fip.available()
  val readed               = new Array[Byte](size)
  fip.read(readed)
  assert(new String(readed, "UTF-8") equals "Hello World")
}

deleteFile := {
  if (f.exists()) {
    f.delete()
  }
}

constructorFileAppendTrueCheck := {
  val fip: FileInputStream = new FileInputStream(f)
  val size                 = fip.available()
  val readed               = new Array[Byte](size)
  fip.read(readed)
  assert(new String(readed, "UTF-8") equals "test Hello World")
}
