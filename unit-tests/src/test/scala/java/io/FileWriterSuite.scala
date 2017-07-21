package java.io

object FileWriterSuite extends tests.Suite {

  test("write to new file") {
    val path    = "/tmp/new.file"
    val toWrite = "something"

    val fw = new FileWriter(path)
    fw.write(toWrite)
    fw.close()

    val buffer    = new Array[Char](20)
    val fr        = new FileReader(path)
    val charsRead = fr.read(buffer)
    assert(charsRead == toWrite.length)
    assert(buffer.take(charsRead).mkString == toWrite)
    fr.close()
  }
}
