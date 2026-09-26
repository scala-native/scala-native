object Main {
  def main(args: Array[String]): Unit = {
    IsDirectoryTest.run()
    IsFileTest.run()
    IsHiddenTest.run()
    LastModifiedTest.run()
    LengthTest.run()
    ListTest.run()
    MkdirTest.run()
    RenameToTest.run()
    LinksTest.run()
    SetLastModifiedTest.run()
    SetReadOnlyTest.run()
    SetExecutableTest.run()
    SetReadableTest.run()
    SetWritableTest.run()
  }
}
