package java.io

object PushbackReaderSuite extends tests.Suite {
  test("PushbackReader can unread characters") {
    val reader =
      new InputStreamReader(new ByteArrayInputStream(Array(1, 2, 3)))
    val pushbackReader = new PushbackReader(reader)

    assert(pushbackReader.read() == 1)
    pushbackReader.unread(1)
    assert(pushbackReader.read() == 1)
    pushbackReader.unread(5)
    assert(pushbackReader.read() == 5)
    assert(pushbackReader.read() == 2)
    assert(pushbackReader.read() == 3)
    pushbackReader.unread(0)
    assert(pushbackReader.read() == 0)
    assert(pushbackReader.read() == -1)
  }
}
