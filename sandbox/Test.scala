import scala.scalanative.native._, stdio._, Nat._, stdlib._
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.FileDescriptor
import java.io.CFile
import java.io.IOException
import java.io.FileNotFoundException

import scala.collection.mutable

object FileIOTest {

  //thos constants should always be synced with the one from JVMChecks.scala
  val fileName = "testFile"
  val f        = new File(fileName)
  val fHidden  = new File(".testFile")

  val arbitraryTime = 1234000

  val testText     = "Hello World !"
  val testTextSize = testText.getBytes.length

  val sRename = "testRenamed"
  val fRename = new File(sRename)

  val dirName = "testDir"
  val dir     = new File(dirName)
  val dirs    = new File("firstTestDir/secondTestDir/thirdTestDir")

  def main(args: Array[String]): Unit = {

    if (args.size > 0) args(0) match {

      case "filePathTest"                   => filePathTest(f, fileName)
      case "fileNotCreatedDoesNotExists"    => fileNotCreatedDoesNotExists(f)
      case "fileCanBeCreated"               => fileCanBeCreated(f)
      case "fileCanBeDeleted"               => deleteFileTest(f)
      case "isDirTest"                      => isDirTest(dir)
      case "isNotDirTest"                   => isNotDirTest(f)
      case "isDirTestWithEmptyPath"         => isNotDirTest(new File(""))
      case "isFileTest"                     => isFileTest(f)
      case "isNotFileTest"                  => isNotFileTest(dir)
      case "isFileTestWithEmptyPath"        => isNotFileTest(new File(""))
      case "createDirectory"                => createDirectory(dir)
      case "createDirs"                     => createDirs(dirs)
      case "existsTest"                     => existsTest(f)
      case "renameToNative"                 => renameToTest(f, fRename)
      case "renameToRAM"                    => renameToFalse(f, fRename)
      case "setReadOnly"                    => setReadOnlyFile
      case "canWriteTest"                   => canWriteTest(true)
      case "canNotWriteTest"                => canWriteTest(false)
      case "canReadTest"                    => canReadTest(true)
      case "canNotReadTest"                 => canReadTest(false)
      case "isHiddenTest"                   => hidden(fHidden, true)
      case "isNotHiddenTest"                => hidden(f, false)
      case "emptyFileHasLengthZero"         => fileLenghtTest(f, 0)
      case "writtenFileHasCorrectLength"    => fileLenghtTest(f, 13)
      case "canNotCreateTwoTimeTheSameFile" => canNotCreateTwoTimeTheSameFile()
      case "isAbsoluteTest"                 => isAbsoluteTest
      case "getAbsolutePathTest"            => getAbsolutePathTest
      case "getAbsoluteFileTest"            => getAbsoluteFileTest
      case "getCanonicalPathTest"           => getCanonicalPathTest
      case "getParentTest"                  => getParentTest
      case "getParentFileTest"              => getParentFileTest
      case "getRoots"                       => listRootsTest
      case "lastModifiedTest"               => lastModifiedTest
      case "setLastModifiedTest"            => setLastModifiedTest
      case "hashCodeTest"                   => hashCodeTest
      case "equalsTest"                     => equalsTest
      case "getNameTest"                    => getNameTest
      case "compareToTest"                  => compareToTest

      case "owerwriteConstructor"        => constructorWithFileTest()
      case "appendConstructor"           => constructWithAppendTrueTest()
      case "writeToStdoutTest"           => writeToStdoutTest
      case "cannotWriteInADirectoryTest" => cannotWriteInADirectoryTest
      case "cannotWriteInAProtectedFile" => cannotWriteInAProtectedFile

      case "readStringFromFileTest"       => readStringFromFileTest
      case "availableFromFileTest"        => availableFromFileTest
      case "cannotReadFromAProtectedFile" => cannotReadFromAProtectedFile
      case "skipOneByteTest"              => skipOneByteTest
      case "skipExceptionsTest"           => skipExceptionsTest
      case "stdinReadTest"                => stdinReadTest
      case "stdinAvailableNoInput"        => stdinAvailableTest(0)
      //the + 1 accounts for the '/0' put at the end of the string in bash
      case "stdinAvailableWithInput" => stdinAvailableTest(testTextSize + 1)
      case "stdinSkipTest"           => stdinSkipTest

      case "assertFalseTest" => assertFalseTest()
      case _                 => assertFalseTest
    } else sandboxTests()

  }

//---------------- File tests --------------------//

  def filePathTest(f: File, s: String) = assert(f.getPath() equals s)

  def fileNotCreatedDoesNotExists(f: File) = assert(!f.exists())

  def fileCanBeCreated(f: File) = assert(f.createNewFile())

  def deleteFileTest(f: File) = assert(f.delete())

  def isDirTest(dir: File) = assert(dir.isDirectory())

  def isNotDirTest(file: File) = assert(!file.isDirectory())

  def isFileTest(file: File) = assert(file.isFile())

  def isNotFileTest(file: File) = assert(!file.isFile())

  def createDirectory(dir: File) = assert(dir.mkdir())

  def createDirs(dirs: File) = assert(dirs.mkdirs())

  def existsTest(f: File) = assert(f.exists())

  def renameToTest(orig: File, dest: File) = {
    assert(orig.exists())
    assert(orig.renameTo(dest))
  }

  def renameToFalse(orig: File, dest: File) = {
    assert(!orig.exists())
    assert(!dest.exists())
    assert(!orig.renameTo(dest))
  }

  def setReadOnlyFile() = {
    assert(f.createNewFile())
    assert(f.canWrite())
    assert(f.setReadOnly())
  }

  def canWriteTest(condition: Boolean) =
    assert(if (condition) f.canWrite() else !f.canWrite())

  def canReadTest(condition: Boolean) =
    assert(if (condition) f.canRead() else !f.canRead())

  def hidden(f: File, condition: Boolean) =
    assert(f.exists && (if (condition) f.isHidden() else !f.isHidden()))

  def fileLenghtTest(f: File, size: Int) = assert(f.length() == size)

  def isAbsoluteTest() = {
    val absolu  = new File("/testFile")
    val nAbsolu = new File(f.getPath)
    assert(absolu.isAbsolute())
    assert(!nAbsolu.isAbsolute)
  }

  def getAbsolutePathTest() = {
    val absPath   = f.getAbsolutePath
    val fopStdout = new FileOutputStream(FileDescriptor.out)
    fopStdout.write(absPath.getBytes)
    fopStdout.close()
  }

  def getAbsoluteFileTest() = {
    val fAbs      = f.getAbsoluteFile()
    val absPath   = f.getAbsolutePath
    val fopStdout = new FileOutputStream(FileDescriptor.out)
    fopStdout.write(absPath.getBytes)
    fopStdout.close()
  }

  def getCanonicalPathTest() = {
    val nCanonPath = "/sandbox/../" + f.getPath
    val nCanonFile = new File(nCanonPath)
    val canonPath  = nCanonFile.getCanonicalPath()
    assert(canonPath equals "/" + f.getPath)
  }

  def getParentTest() = {
    val childFile = new File(dirName + "/" + fileName)
    assert(childFile.getParent equals dirName)
    assert(f.getParent == null)
  }

  def getParentFileTest() = {
    val childFile = new File(dirName + "/" + fileName)
    assert(childFile.getParentFile.getPath equals dirName)
    assert(f.getParentFile == null)
  }

  def listRootsTest() = {
    val roots = File.listRoots()
    assert(roots.length == 1)
    assert(roots(0).getPath equals "/")
  }

  def lastModifiedTest() = {
    assert(f.lastModified == arbitraryTime)
  }

  def setLastModifiedTest() = {
    f.setLastModified(arbitraryTime)
  }

  def canNotCreateTwoTimeTheSameFile() = {
    assert(f.createNewFile())
    assert(!f.createNewFile())
  }

  def hashCodeTest() = {
    val fSame  = new File(fileName)
    val fOther = new File("notTestFile")
    assert(f.hashCode() == fSame.hashCode)
    assert(fOther.hashCode != f.hashCode)
  }

  def equalsTest() = {
    val fSame  = new File(fileName)
    val fOther = new File("notTestFile")
    assert(f.equals(fSame))
    assert(!fOther.equals(f))
  }

  def getNameTest() = {
    assert(f.getName equals fileName)
    val dirAndFile = new File(dirName + "/" + fileName)
    assert(dirAndFile.getName equals fileName)
    assert(dirs.getName equals "thirdTestDir")
  }

  //in the eventuality of a port to windows, need lowercase only for this test
  def compareToTest() = {
    val f1 = new File("testfile")
    val f2 = new File("filetest")
    val f3 = new File("dir/testfile")
    //values obtained by trying this test on JVM
    //but they are trivial, it is just the difference
    //in the alphabetical order
    val oneThree = 16
    val oneTwo   = 14
    val twoThree = 2
    assert((f1 compareTo f1) == 0)
    assert((f1 compareTo f2) == oneTwo)
    assert((f1 compareTo f3) == oneThree)
    assert((f2 compareTo f1) == -oneTwo)
    assert((f2 compareTo f2) == 0)
    assert((f2 compareTo f3) == twoThree)
    assert((f3 compareTo f1) == -oneThree)
    assert((f3 compareTo f2) == -twoThree)
    assert((f3 compareTo f3) == 0)
  }

//---------------- FileOutputStream tests --------//

  //when passing only a file, it is in mode WRONLY, so overwriting.
  def constructorWithFileTest() = {
    val fop     = new FileOutputStream(f)
    val content = testText
    fop.write(content.getBytes())
    fop.close()
  }

  def constructWithAppendTrueTest() = {
    val fop     = new FileOutputStream(f, true)
    val content = testText
    fop.write(content.getBytes())
    fop.close()
  }

  def writeToStdoutTest() = {
    val fop     = new FileOutputStream(FileDescriptor.out)
    val content = testText
    fop.write(content.getBytes())
    fop.close()
  }

  def cannotWriteInADirectoryTest() = {
    try {
      val fop = new FileOutputStream(dir)
      assert(false)
    } catch {
      case e: FileNotFoundException => assert(true)
      case _: Throwable             => assert(false)
    }
  }

  def cannotWriteInAProtectedFile() =
    try {
      val fop = new FileOutputStream(f)
      fop.write(testText.getBytes)
      fop.close
      assert(false)
    } catch {
      case e: FileNotFoundException => assert(true)
      case _: Throwable             => assert(false)
    }

//---------------- FileInputStream tests --------//

  def readStringFromFileTest() = {
    val fip = new FileInputStream(f)
    val b   = new Array[Byte](testTextSize)
    fip.read(b)
    assert(new String(b, "UTF-8") equals testText)
  }

  def availableFromFileTest = {
    val fip = new FileInputStream(f)
    assert(testTextSize equals fip.available())
  }

  def cannotReadFromAProtectedFile() =
    try {
      val fip = new FileInputStream(f)
      val b   = new Array[Byte](testTextSize)
      fip.read(b)
      assert(false)
    } catch {
      case e: FileNotFoundException => assert(true)
      case _: Throwable             => assert(false)
    }

  def skipOneByteTest() = {
    val fip   = new FileInputStream(f)
    val hello = new Array[Byte]("Hello".getBytes.length)
    val world = new Array[Byte]("World !".getBytes.length)
    fip.read(hello)
    assert(new String(hello, "UTF-8") equals "Hello")
    fip.skip(1)
    fip.read(world)
    assert(new String(world, "UTF-8") equals "World !")
  }

  def skipExceptionsTest() =
    try {
      val fip = new FileInputStream(f)
      fip.skip(-1)
      assert(false)
    } catch {
      case e: IOException => assert(true)
      case _: Throwable   => assert(false)
    }

  def stdinReadTest = {
    val fipStdin = new FileInputStream(FileDescriptor.in)
    val bytes    = new Array[Byte](testTextSize)
    fipStdin.read(bytes)
    assert(new String(bytes, "UTF-8") equals testText)
  }

  def stdinAvailableTest(nmbr: Long) = {
    val fipStdin = new FileInputStream(FileDescriptor.in)
    assert(fipStdin.available() == nmbr)
  }

  def stdinSkipTest = {
    val fip   = new FileInputStream(FileDescriptor.in)
    val hello = new Array[Byte]("Hello".getBytes.length)
    val world = new Array[Byte]("World !".getBytes.length)
    fip.read(hello)
    assert(new String(hello, "UTF-8") equals "Hello")
    fip.skip(1)
    fip.read(world)
    assert(new String(world, "UTF-8") equals "World !")
  }

//-------------- Misc. -----------------------//

  def assertFalseTest() = {
    println(
      "\n[warning] A wrong command was entered, please check your scripts.\n")
    assert(false)
  }

  def sandboxTests() = {
    println("Hello World")
  }
}
