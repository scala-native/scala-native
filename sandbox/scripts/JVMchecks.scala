import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.FileDescriptor

object JVMchecks {

  //those constants should always be synced with the one of Test.scala.
  val fileName      = "testFile"
  val f: File       = new File("testFile")
  val fHidden: File = new File(".testFile")
  val dir: File     = new File("testDir")
  val sRename       = "testRenamed"
  val fRename       = new File(sRename)

  //getting lastModified return a value truncated to the second,
  //so this value should always contain 3 zeros at the end.
  val arbitraryTime = 1234000

  val testText     = "Hello World !"
  val testTextSize = testText.getBytes.length

  def main(args: Array[String]): Unit = {

    if (args.size > 0) args(0) match {
      case "createNewFile"        => createNewFile(f)
      case "createNewDir"         => createNewDir
      case "isDir"                => isDir
      case "deleteFile"           => deleteFile(f)
      case "deleteHiddenFile"     => deleteFile(fHidden)
      case "deleteDir"            => deleteDir
      case "mkdirsCheck"          => mkdirsCheck
      case "fileExistsCheck"      => fileExistsCheck
      case "fileDontExistsCheck"  => fileDontExistsCheck
      case "renameToCheck"        => renameToCheck(f, fRename)
      case "isReadOnlyCheck"      => isReadOnlyCheck
      case "setReadOnly"          => setReadOnlyFile
      case "setReadableFalse"     => setReadableFalse
      case "writeAFileWithText"   => writeAFileWithText
      case "createHiddenFile"     => createNewFile(fHidden)
      case "writeToStdout"        => writeToStdout(testText)
      case "getAbsolutePathCheck" => compareWithStdin(f.getAbsolutePath)
      case "setLastModifiedTime"  => setLastModifiedTime
      case "lastModifiedCheck"    => lastModifiedCheck

      case "constructorFileAppendTrueCheck" => constructorFileAppendTrueCheck
      case "constructorFileCheck"           => constructorFileCheck
      case "createFileWithAlreadyWrittenText" =>
        createFileWithAlreadyWrittenText
      case "checkStdin" => checkStdin
      case _            => assertFalseTest
    } else sandbox
  }

  def createNewDir = {
    assert(dir.mkdir())
  }

  def isDir() = assert(dir.isDirectory())

  def deleteDir = {
    if (dir.exists()) {
      dir.list().map(idx => new File(dir.getPath(), idx).delete())
      dir.delete()
    }
  }

  def mkdirsCheck = {
    val f1 = new File("firstTestDir")
    val f2 = new File(f1, "secondTestDir")
    val f3 = new File(f2, "thirdTestDir")
    assert(f3.isDirectory())
    assert(f2.isDirectory())
    assert(f1.isDirectory())
    assert(f3.delete())
    assert(f2.delete())
    assert(f1.delete())
  }

  def createNewFile(f: File) = {
    assert(f.createNewFile())
  }

  def fileExistsCheck = {
    assert(f.exists())
  }

  def fileDontExistsCheck = {
    assert(!f.exists())
  }

  def renameToCheck(orig: File, dest: File) = {
    assert(!orig.exists)
    assert(dest.exists)
    assert(dest.getPath equals sRename)
    assert(dest.delete)
  }

  def isReadOnlyCheck = {
    assert(!f.canWrite())
    assert(f.delete())
  }

  def setReadOnlyFile = {
    assert(f.createNewFile)
    assert(f.setReadOnly)
  }

  def setReadableFalse = {
    if (!f.exists) {
      assert(f.createNewFile)
    }
    f.setReadable(false)
  }

  def writeAFileWithText = {
    val fop = new FileOutputStream(f)
    fop.write(testText.getBytes)
    fop.close
  }

  def deleteFile(f: File) = {
    if (f.exists()) {
      f.delete()
    }
  }

  def setLastModifiedTime = {
    assert(f.createNewFile)
    f.setLastModified(arbitraryTime)
  }

  def lastModifiedCheck = {
    assert(f.lastModified == arbitraryTime)
  }

  def assertFalseTest = {
    println("A wrong command was entered, please check your scripts.")
    assert(false)
  }

  def createFileWithAlreadyWrittenText = {
    f.createNewFile()
    val fop: FileOutputStream = new FileOutputStream(f, false)
    fop.write("test".getBytes())
    fop.close()
  }

  def constructorFileCheck = {
    assert(f.exists)
    val fip: FileInputStream = new FileInputStream(f)
    val size                 = fip.available()
    val readed               = new Array[Byte](size)
    fip.read(readed)
    assert(new String(readed, "UTF-8") equals testText)
  }

  def constructorFileAppendTrueCheck = {
    val fip: FileInputStream = new FileInputStream(f)
    val size                 = fip.available()
    val readed               = new Array[Byte](size)
    fip.read(readed)
    assert(new String(readed, "UTF-8") equals "test" + testText)
  }

  def checkStdin = {
    val b        = new Array[Byte](testTextSize)
    val fipStdin = new FileInputStream(FileDescriptor.in)
    fipStdin.read(b)
    assert(new String(b, "UTF-8") equals testText)
  }

  def writeToStdout(toWrite: String) = {
    val fop   = new FileOutputStream(FileDescriptor.out)
    val bytes = toWrite.getBytes
    fop.write(bytes)
  }

  def compareWithStdin(path: String) = {
    val b        = new Array[Byte](path.getBytes.length)
    val fipStdin = new FileInputStream(FileDescriptor.in)
    fipStdin.read(b)
    assert(new String(b, "UTF-8") equals path)
  }

  def sandbox = {
    println("Hello World !")
  }
}
