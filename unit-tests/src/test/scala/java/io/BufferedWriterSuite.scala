package java.io

object BufferedWriterSuite extends tests.Suite {

  test(
    "Creating a `BufferedWriter` with a buffer of size 0 throws an exception") {
    val writer = new OutputStreamWriter(new ByteArrayOutputStream)
    assertThrows[IllegalArgumentException] {
      new BufferedWriter(writer, -1)
    }
  }

  test("Can write small chunks to a `BufferedWriter`") {
    val stream = new ByteArrayOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stream))
    val string = "Hello, world!"
    writer.write(string)
    writer.flush()
    assert(stream.toString == string)
  }

  test("Can write a chunk larger than buffer size to a `BufferedWriter`") {
    val stream = new ByteArrayOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stream), 1)
    val string = "Hello, world!"
    writer.write(string)
    writer.flush()
    assert(stream.toString == string)
  }

  test("Closing twice is harmless") {
    val stream = new ByteArrayOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stream))
    writer.close()
    writer.close()
  }

}
