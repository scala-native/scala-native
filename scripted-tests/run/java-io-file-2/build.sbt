import Files._
import java.nio.file.{Files => NioFiles}

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.12"

nativeLinkStubs := true // DateFormatSymbols

lazy val setupTests = taskKey[Unit]("")

setupTests := {

  IO.touch(readableFile)
  readableFile.setReadable(true)
  assert(readableFile.exists())
  assert(readableFile.canRead())

  IO.touch(unreadableFile)
  unreadableFile.setReadable(false)
  assert(unreadableFile.exists())
  assert(!unreadableFile.canRead())

  IO.createDirectory(readableDirectory)
  readableDirectory.setReadable(true)
  assert(readableDirectory.exists())
  assert(readableDirectory.canRead())

  IO.createDirectory(unreadableDirectory)
  unreadableDirectory.setReadable(false)
  assert(unreadableDirectory.exists())
  assert(!unreadableDirectory.canRead())

  IO.touch(executableFile)
  executableFile.setExecutable(true)
  assert(executableFile.exists())
  assert(executableFile.canExecute())

  IO.touch(unexecutableFile)
  unexecutableFile.setExecutable(false)
  assert(unexecutableFile.exists())
  assert(!unexecutableFile.canExecute())

  IO.createDirectory(executableDirectory)
  executableDirectory.setExecutable(true)
  assert(executableDirectory.exists())
  assert(executableDirectory.canExecute())

  IO.createDirectory(unexecutableDirectory)
  unexecutableDirectory.setExecutable(false)
  assert(unexecutableDirectory.exists())
  assert(!unexecutableDirectory.canExecute())

  IO.touch(writableFile)
  writableFile.setWritable(true)
  assert(writableFile.exists())
  assert(writableFile.canWrite())

  IO.touch(unwritableFile)
  unwritableFile.setWritable(false)
  assert(unwritableFile.exists())
  assert(!unwritableFile.canWrite())

  IO.createDirectory(writableDirectory)
  writableDirectory.setWritable(true)
  assert(writableDirectory.exists())
  assert(writableDirectory.canWrite())

  IO.createDirectory(unwritableDirectory)
  unwritableDirectory.setWritable(false)
  assert(unwritableDirectory.exists())
  assert(!unwritableDirectory.canWrite())

  assert(fileA.compareTo(fileB) < 0)
  assert(fileA.compareTo(fileA) == 0)
  assert(fileB.compareTo(fileA) > 0)
  assert(fileB.compareTo(fileB) == 0)

  // Make sure that the files doesn't already exist, and that it can be created.
  assert(!willBeCreatedFile.exists())
  assert(willBeCreatedFile.createNewFile())
  assert(willBeCreatedFile.exists())
  assert(willBeCreatedFile.delete())
  assert(!willBeCreatedFile.exists())

  assert(!willBeCreatedDirectory.exists())
  assert(willBeCreatedDirectory.mkdir())
  assert(!willBeCreatedDirectory.mkdir())
  assert(willBeCreatedDirectory.exists())
  assert(willBeCreatedDirectory.delete())
  assert(!willBeCreatedDirectory.exists())

  assert(!nestedWillBeCreatedDirectoryBase.exists())
  assert(!nestedWillBeCreatedDirectory.exists())
  assert(nestedWillBeCreatedDirectory.mkdirs())
  assert(!nestedWillBeCreatedDirectory.mkdir())
  assert(nestedWillBeCreatedDirectory.exists())
  IO.delete(nestedWillBeCreatedDirectoryBase)
  assert(!nestedWillBeCreatedDirectoryBase.exists())

  IO.touch(willBeDeletedFile)
  IO.createDirectory(willBeDeletedDirectory)
  assert(willBeDeletedFile.exists())
  assert(willBeDeletedDirectory.exists())

  IO.touch(existingHiddenFile)
  IO.createDirectory(existingHiddenDirectory)
  assert(currentDirectory.isHidden())
  assert(existingHiddenFile.exists())
  assert(existingHiddenFile.isHidden())
  assert(existingHiddenDirectory.exists())
  assert(existingHiddenDirectory.isHidden())
  assert(!nonexistentHiddenFile.exists())
  assert(nonexistentHiddenFile.isHidden())

  IO.write(fileWith3Bytes, Array[Byte](1, 2, 3))
  assert(fileWith3Bytes.exists())
  assert(fileWith3Bytes.length() == 3L)

  IO.createDirectory(nonEmptyDirectory)
  IO.touch(firstChildFile)
  IO.touch(secondChildFile)
  IO.createDirectory(thirdChildDirectory)
  val listedFiles = nonEmptyDirectory.list().sorted
  assert(listedFiles.length == 3)
  assert(listedFiles(0) == firstChildFile.getName)
  assert(listedFiles(1) == secondChildFile.getName)
  assert(listedFiles(2) == thirdChildDirectory.getName)

  IO.touch(willBeRenamedFrom)
  assert(willBeRenamedFrom.exists)
  assert(!willBeRenamedTo.exists)

  IO.createDirectory(directoryLinkedTo)
  NioFiles.createSymbolicLink(linkToDirectory.toPath, directoryLinkedTo.toPath)
  assert(directoryLinkedTo.exists)
  assert(linkToDirectory.exists)
  assert(linkToDirectory.getCanonicalPath == directoryLinkedTo.getCanonicalPath)
  assert(linkToDirectory.getName != directoryLinkedTo.getName)

  assert(canon0F.getCanonicalPath == canon0N)
  assert(canon1F.getCanonicalPath == canon1N)
  assert(canon2F.getCanonicalPath == canon2N)
  assert(canon3F.getCanonicalPath == canon3N)
  assert(canon4F.getCanonicalPath == canon4N)
  assert(canon5F.getCanonicalPath == canon5N)

  if (java.io.File.separator == "/") {
    assert(absoluteUnixStyle.isAbsolute)
    assert(!absoluteWinStyle0.isAbsolute)
    assert(!absoluteWinStyle1.isAbsolute)
    assert(!absoluteWinStyle2.isAbsolute)
  } else {
    assert(!absoluteUnixStyle.isAbsolute)
    assert(absoluteWinStyle0.isAbsolute)
    assert(absoluteWinStyle1.isAbsolute)
    assert(absoluteWinStyle2.isAbsolute)
  }

  assert(!relative0.isAbsolute)
  assert(!relative1.isAbsolute)
  assert(!relative2.isAbsolute)

  assert(children0.getParent == expectedParent0)
  assert(children1.getParent == expectedParent1)
  assert(children2.getParent == expectedParent2)
  assert(children3.getParent == expectedParent3)
  assert(children4.getParent == expectedParent4)
  assert(children5.getParentFile == expectedParent5)

  IO.createDirectory(existingTempTarget)
  assert(existingTempTarget.exists)
  assert(existingTempTarget.isDirectory)
  assert(!nonexistingTempTarget.exists)

  IO.touch(fileWithLastModifiedSet)
  assert(fileWithLastModifiedSet.exists())
  fileWithLastModifiedSet.setLastModified(expectedLastModified)
  assert(fileWithLastModifiedSet.lastModified == expectedLastModified)
  IO.touch(willBeSetLastModified)
  assert(willBeSetLastModified.exists)

  IO.touch(willBeSetReadOnlyFile)
  assert(willBeSetReadOnlyFile.exists)
  assert(willBeSetReadOnlyFile.setReadable(true, false))
  assert(willBeSetReadOnlyFile.setWritable(true, false))
  assert(willBeSetReadOnlyFile.setExecutable(true, false))

  IO.createDirectory(willBeSetReadOnlyDirectory)
  assert(willBeSetReadOnlyDirectory.exists)
  assert(willBeSetReadOnlyDirectory.setReadable(true, false))
  assert(willBeSetReadOnlyDirectory.setWritable(true, false))
  assert(willBeSetReadOnlyDirectory.setExecutable(true, false))

  IO.touch(willBeSetExecutableFile)
  assert(willBeSetExecutableFile.exists)
  assert(willBeSetExecutableFile.setReadable(false, false))
  assert(willBeSetExecutableFile.setWritable(false, false))
  assert(willBeSetExecutableFile.setExecutable(false, false))

  IO.createDirectory(willBeSetExecutableDirectory)
  assert(willBeSetExecutableDirectory.exists)
  assert(willBeSetExecutableDirectory.setReadable(false, false))
  assert(willBeSetExecutableDirectory.setWritable(false, false))
  assert(willBeSetExecutableDirectory.setExecutable(false, false))

  IO.touch(willBeSetReadableFile)
  assert(willBeSetReadableFile.exists)
  assert(willBeSetReadableFile.setReadable(false, false))
  assert(willBeSetReadableFile.setWritable(false, false))
  assert(willBeSetReadableFile.setExecutable(false, false))

  IO.createDirectory(willBeSetReadableDirectory)
  assert(willBeSetReadableDirectory.exists)
  assert(willBeSetReadableDirectory.setReadable(false, false))
  assert(willBeSetReadableDirectory.setWritable(false, false))
  assert(willBeSetReadableDirectory.setExecutable(false, false))

  IO.touch(willBeSetWritableFile)
  assert(willBeSetWritableFile.exists)
  assert(willBeSetWritableFile.setReadable(false, false))
  assert(willBeSetWritableFile.setWritable(false, false))
  assert(willBeSetWritableFile.setExecutable(false, false))

  IO.createDirectory(willBeSetWritableDirectory)
  assert(willBeSetWritableDirectory.exists)
  assert(willBeSetWritableDirectory.setReadable(false, false))
  assert(willBeSetWritableDirectory.setWritable(false, false))
  assert(willBeSetWritableDirectory.setExecutable(false, false))

  assert(!nonexistentFile.exists())
  assert(!nonexistentDirectory.exists())

}
