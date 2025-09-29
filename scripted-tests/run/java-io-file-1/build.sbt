import java.nio.file.{Files => NioFiles}

import Files._
import Utils._

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

lazy val setupTests = taskKey[Unit]("")

setupTests := {

  IO.touch(readableFile)
  assert(readableFile.setReadable(true))
  assert(readableFile.exists())
  assert(readableFile.canRead())

  IO.touch(unreadableFile)
  // setReadable(false) not possible on Windows
  assertOsSpecific(
    unreadableFile.setReadable(false),
    "unreadableFile.setReadable(false)"
  )(onUnix = true, onWindows = false)
  assert(unreadableFile.exists())
  assertOsSpecific(
    unreadableFile.canRead(),
    "!unreadableFile.canRead()"
  )(onUnix = false, onWindows = true)

  IO.createDirectory(readableDirectory)
  assert(readableDirectory.setReadable(true))
  assert(readableDirectory.exists())
  assert(readableDirectory.canRead())

  IO.createDirectory(unreadableDirectory)
  assert(unreadableDirectory.exists())
  // setReadable(false) not possible on Windows
  assertOsSpecific(
    unreadableDirectory.setReadable(false),
    "unreadableDirectory.setReadable(false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    unreadableDirectory.canRead(),
    "!unreadableDirectory.canRead()"
  )(onUnix = false, onWindows = true)

  IO.touch(executableFile)
  assert(executableFile.setExecutable(true))
  assert(executableFile.exists())
  assert(executableFile.canExecute())

  IO.touch(unexecutableFile)
  assert(unexecutableFile.exists())
  // setExecutable(false) not possible on Windows
  assertOsSpecific(
    unexecutableFile.setExecutable(false),
    "unexecutableFile.setExecutable(false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    unexecutableFile.canExecute(),
    "!unexecutableFile.canExecute()"
  )(onUnix = false, onWindows = true)
  IO.createDirectory(executableDirectory)
  assert(executableDirectory.setExecutable(true))
  assert(executableDirectory.exists())
  assert(executableDirectory.canExecute())

  IO.createDirectory(unexecutableDirectory)
  assert(unexecutableDirectory.exists())
  // setExecutable(false) not possible on Windows
  assertOsSpecific(
    unexecutableDirectory.setExecutable(false),
    "unexecutableDirectory.setExecutable(false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    unexecutableDirectory.canExecute(),
    "!unexecutableDirectory.canExecute()"
  )(onUnix = false, onWindows = true)

  IO.touch(writableFile)
  assert(writableFile.setWritable(true))
  assert(writableFile.exists())
  assert(writableFile.canWrite())

  IO.touch(unwritableFile)
  assert(unwritableFile.setWritable(false))
  assert(unwritableFile.exists())
  assert(!unwritableFile.canWrite())

  IO.createDirectory(writableDirectory)
  // setWritable directory not possible on Windows
  assertOsSpecific(
    writableDirectory.setWritable(true),
    "writableDirectory.setWritable(true)"
  )(onUnix = true, onWindows = false)
  assert(writableDirectory.exists())
  assert(writableDirectory.canWrite())

  IO.createDirectory(unwritableDirectory)
  assert(unwritableDirectory.exists())
  // setWritable directory not possible on Windows
  assertOsSpecific(
    unwritableDirectory.setWritable(false),
    "unwritableDirectory.setWritable(false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    unwritableDirectory.canWrite(),
    "!unwritableDirectory.canWrite()"
  )(onUnix = false, onWindows = true)

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
  assert(existingHiddenFile.exists())
  assert(existingHiddenDirectory.exists())
  assert(!nonexistentHiddenFile.exists())
  if (Platform.isWindows) {
    Seq(currentDirectory, existingHiddenDirectory, existingHiddenFile)
      .map(_.toPath)
      .foreach(NioFiles.setAttribute(_, "dos:hidden", true.booleanValue()))
  }
  assert(currentDirectory.isHidden())
  assert(existingHiddenFile.isHidden())
  assert(existingHiddenDirectory.isHidden())
  assertOsSpecific(
    nonexistentHiddenFile.isHidden(),
    "nonexistentHiddenFile.isHidden()"
  )(onUnix = true, onWindows = false)
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
  assert(directoryLinkedTo.exists)
  if (!Platform.isWindows) {
    // Symbolic links on Windows are broken, needs admin priviliges
    NioFiles.createSymbolicLink(
      linkToDirectory.toPath,
      directoryLinkedTo.toPath
    )
    assert(linkToDirectory.exists)
    assert(
      linkToDirectory.getCanonicalPath == directoryLinkedTo.getCanonicalPath
    )
    assert(linkToDirectory.getName != directoryLinkedTo.getName)
  }

  assert(canon0F.getCanonicalPath == canon0N)
  assert(canon1F.getCanonicalPath == canon1N)
  assert(canon2F.getCanonicalPath == canon2N)
  assert(canon3F.getCanonicalPath == canon3N)
  assert(canon4F.getCanonicalPath == canon4N)
  assert(canon5F.getCanonicalPath == canon5N)

  assertOsSpecific(
    absoluteUnixStyle.isAbsolute,
    "absoluteUnixStyle.isAbsolute"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    absoluteWinStyle0.isAbsolute,
    "!absoluteWinStyle0.isAbsolute"
  )(onUnix = false, onWindows = true)
  assertOsSpecific(
    absoluteWinStyle1.isAbsolute,
    "!absoluteWinStyle1.isAbsolute"
  )(onUnix = false, onWindows = true)
  assertOsSpecific(
    absoluteWinStyle2.isAbsolute,
    "!absoluteWinStyle2.isAbsolute"
  )(onUnix = false, onWindows = true)

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
  assert(fileWithLastModifiedSet.setLastModified(expectedLastModified))
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
  // Not supported on Windows
  assertOsSpecific(
    willBeSetReadOnlyDirectory.setWritable(true, false),
    "willBeSetReadOnlyDirectory.setWritable(true, false)"
  )(onUnix = true, onWindows = false)
  assert(willBeSetReadOnlyDirectory.setExecutable(true, false))

  IO.touch(willBeSetExecutableFile)
  assert(willBeSetExecutableFile.exists)
  assert(willBeSetExecutableFile.setWritable(false, false))
  // Not supported on Windows
  assertOsSpecific(
    willBeSetExecutableFile.setReadable(false, false),
    "willBeSetExecutableFile.setReadable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetExecutableFile.setExecutable(false, false),
    "willBeSetExecutableFile.setExecutable(false, false)"
  )(onUnix = true, onWindows = false)

  IO.createDirectory(willBeSetExecutableDirectory)
  assert(willBeSetExecutableDirectory.exists)
  // Not supported on Windows
  assertOsSpecific(
    willBeSetExecutableDirectory.setReadable(false, false),
    "willBeSetExecutableDirectory.setReadable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetExecutableDirectory.setWritable(false, false),
    "willBeSetExecutableDirectory.setWritable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetExecutableDirectory.setExecutable(false, false),
    "willBeSetExecutableDirectory.setExecutable(false, false)"
  )(onUnix = true, onWindows = false)

  IO.touch(willBeSetReadableFile)
  assert(willBeSetReadableFile.exists)
  assert(willBeSetReadableFile.setWritable(false, false))
  // Not supported on Windows
  assertOsSpecific(
    willBeSetReadableFile.setReadable(false, false),
    "willBeSetReadableFile.setReadable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetReadableFile.setExecutable(false, false),
    "willBeSetReadableFile.setExecutable(false, false)"
  )(onUnix = true, onWindows = false)

  IO.createDirectory(willBeSetReadableDirectory)
  assert(willBeSetReadableDirectory.exists)
  // Not supported on Windows
  assertOsSpecific(
    willBeSetReadableDirectory.setReadable(false, false),
    "willBeSetReadableDirectory.setReadable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetReadableDirectory.setWritable(false, false),
    "willBeSetReadableDirectory.setWritable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetReadableDirectory.setExecutable(false, false),
    "willBeSetReadableDirectory.setExecutable(false, false)"
  )(onUnix = true, onWindows = false)

  IO.touch(willBeSetWritableFile)
  assert(willBeSetWritableFile.exists)
  assert(willBeSetWritableFile.setWritable(false, false))
  // Not supported on Windows
  assertOsSpecific(
    willBeSetWritableFile.setReadable(false, false),
    "willBeSetWritableFile.setReadable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetWritableFile.setExecutable(false, false),
    "willBeSetWritableFile.setExecutable(false, false)"
  )(onUnix = true, onWindows = false)

  IO.createDirectory(willBeSetWritableDirectory)
  assert(willBeSetWritableDirectory.exists)
  // Not supported on Windows
  assertOsSpecific(
    willBeSetWritableDirectory.setReadable(false, false),
    "willBeSetWritableDirectory.setReadable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetWritableDirectory.setWritable(false, false),
    "willBeSetWritableDirectory.setWritable(false, false)"
  )(onUnix = true, onWindows = false)
  assertOsSpecific(
    willBeSetWritableDirectory.setExecutable(false, false),
    "willBeSetWritableDirectory.setExecutable(false, false)"
  )(onUnix = true, onWindows = false)

  assert(!nonexistentFile.exists())
  assert(!nonexistentDirectory.exists())

}
