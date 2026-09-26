object LengthTest {
  import Files._

  def run(): Unit = {
    assert(fileWith3Bytes.exists())
    assert(fileWith3Bytes.length() == 3L)

    assert(!nonexistentFile.exists())
    assert(nonexistentFile.length() == 0L)
  }
}
