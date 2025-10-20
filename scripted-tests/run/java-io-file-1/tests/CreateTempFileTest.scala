import java.io.{File, IOException}

object CreateTempFileTest {
  import Files.*
  def main(args: Array[String]): Unit = {
    assert(existingTempTarget.exists)
    assert(existingTempTarget.isDirectory)
    assert(!nonexistingTempTarget.exists)

    val tmp0 = File.createTempFile("tmp", null)
    assert(tmp0.exists)
    assert(tmp0.getName.startsWith("tmp"))
    assert(tmp0.getName.endsWith(".tmp"))

    val tmp1 = File.createTempFile("foo", "bar")
    assert(tmp1.exists)
    assert(tmp1.getName.startsWith("foo"))
    assert(tmp1.getName.endsWith("bar"))

    val tmp2 = File.createTempFile("foo", "bar", null)
    assert(tmp2.exists)
    assert(tmp2.getName.startsWith("foo"))
    assert(tmp2.getName.endsWith("bar"))

    val tmp3 = File.createTempFile("foo", "bar", existingTempTarget)
    assert(tmp3.exists)
    assert(tmp3.getName.startsWith("foo"))
    assert(tmp3.getName.endsWith("bar"))
    assert(tmp3.getParentFile == existingTempTarget)

    val exceptionThrown =
      try {
        File.createTempFile("foo", "bar", nonexistingTempTarget); false
      } catch {
        case _: IOException => true
      }
    assert(exceptionThrown)

  }
}
