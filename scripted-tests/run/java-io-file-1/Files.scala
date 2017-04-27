import java.io.File

object Files {

  val emptyNameFile = new File("")

  val executableFile        = new File("executableFile.txt")
  val unexecutableFile      = new File("unexecutableFile.txt")
  val executableDirectory   = new File("executableDirectory")
  val unexecutableDirectory = new File("unexecutableDirectory")

  val readableFile        = new File("readableFile.txt")
  val unreadableFile      = new File("unreadableFile.txt")
  val readableDirectory   = new File("readableDirectory")
  val unreadableDirectory = new File("readableleDirectory")

  val writableFile        = new File("writableFile.txt")
  val unwritableFile      = new File("unwritableFile.txt")
  val writableDirectory   = new File("writableDirectory")
  val unwritableDirectory = new File("unwritableDirectory")

  // Used to test `File.compareTo`
  val fileA = new File("A")
  val fileB = new File("B")

  val willBeCreatedFile                = new File("willBeCreatedFile.txt")
  val willBeCreatedDirectory           = new File("willBeCreatedDirectory")
  val nestedWillBeCreatedDirectoryBase = new File("d0")
  val nestedWillBeCreatedDirectory = new File(
    nestedWillBeCreatedDirectoryBase,
    "d1/nestedwillBeCreatedDirectory")
  val willBeDeletedFile      = new File("willBeDeletedFile.txt")
  val willBeDeletedDirectory = new File("willBeDeletedDirectory")

  val nonexistentFile      = new File("nonexistentFile.txt")
  val nonexistentDirectory = new File("nonexistentDirectory")

  val currentDirectory        = new File(".")
  val existingHiddenFile      = new File(".existingFile.txt")
  val nonexistentHiddenFile   = new File(".nonexistentFile.txt")
  val existingHiddenDirectory = new File(".existingDirectory")

  val fileWith3Bytes = new File("fileWith3Bytes.bin")

  val nonEmptyDirectory   = new File("nonEmptyDirectory")
  val firstChildFile      = new File(nonEmptyDirectory, "0firstChildFile")
  val secondChildFile     = new File(nonEmptyDirectory, "1secondChildFile")
  val thirdChildDirectory = new File(nonEmptyDirectory, "2thirdChildDirectory")

  val willBeRenamedFrom = new File("willBeRenamedFrom.txt")
  val willBeRenamedTo   = new File("willBeReanmedTo.txt")

  val directoryLinkedTo = new File("directoryLinkedTo")
  val linkToDirectory   = new File("linkToDirectory")

  // Those files are there to test the canonical name
  val canon0F = new File("/.")
  val canon0N = "/"
  val canon1F = new File("/../")
  val canon1N = "/"
  val canon2F = new File("/foo/./bar")
  val canon2N = "/foo/bar"
  val canon3F = new File("/foo/../bar")
  val canon3N = "/bar"
  val canon4F = new File("/foo/../../bar")
  val canon4N = "/../bar"
  val canon5F = new File("/foo/../../../bar")
  val canon5N = "/bar"

  // To test absolute names
  val absoluteUnixStyle = new File("/foo/bar")
  val absoluteWinStyle0 = new File("\\\\hello")
  val absoluteWinStyle1 = new File("C:/foobar")
  val absoluteWinStyle2 = new File("Z:\\hello")
  val relative0         = new File("")
  val relative1         = new File("\\")
  val relative2         = new File("../")

  val children0       = new File("/foo/bar")
  val expectedParent0 = "/foo"
  val children1       = new File("foo/bar")
  val expectedParent1 = "foo"
  val children2       = new File("/foobar")
  val expectedParent2 = "/"
  val children3       = new File("foobar")
  val expectedParent3 = null
  val children4       = new File("")
  val expectedParent4 = null
  val expectedParent5 = new File("foo")
  val children5       = new File(expectedParent5, "bar")

  val expectedLastModified    = 1482494812000L
  val fileWithLastModifiedSet = new File("fileWithLastModifiedSet")
  val willBeSetLastModified   = new File("willBeSetLastModified")

  val willBeSetReadOnlyFile      = new File("willBeSetReadOnlyFile")
  val willBeSetReadOnlyDirectory = new File("willBeSetReadOnlyDirectory")

  val willBeSetExecutableFile      = new File("willBeSetExecutableFile")
  val willBeSetExecutableDirectory = new File("willBeSetExecutableDirectory")

  val willBeSetReadableFile      = new File("willBeSetReadableFile")
  val willBeSetReadableDirectory = new File("willBeSetReadableDirectory")

  val willBeSetWritableFile      = new File("willBeSetWritableFile")
  val willBeSetWritableDirectory = new File("willBeSetWritableDirectory")

  val existingTempTarget    = new File("existingTempTarget")
  val nonexistingTempTarget = new File("nonexistingTempTarget")

}
