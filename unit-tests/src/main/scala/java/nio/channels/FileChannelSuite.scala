package java.nio.channels

import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import java.io.File

object FileChannelSuite extends tests.Suite {
  test("A FileChannel can read from a file") {
    withTemporaryDirectory { dir =>
      val f     = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      Files.write(f, bytes)
      assert(Files.getAttribute(f, "size") == 5)

      val channel = FileChannel.open(f)
      val buffer  = ByteBuffer.allocate(5)
      while (channel.read(buffer) > 0) {}

      buffer.rewind()
      var i = 0
      while (i < bytes.length) {
        assert(buffer.get() == bytes(i))
        i += 1
      }
    }
  }

  test("A FileChannel can write to a file") {
    withTemporaryDirectory { dir =>
      val f       = dir.resolve("f")
      val bytes   = Array.apply[Byte](1, 2, 3, 4, 5)
      val src     = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f)
      while (src.remaining() > 0) channel.write(src)

      val in = Files.newInputStream(f)
      var i  = 0
      while (i < bytes.length) {
        assert(in.read() == bytes(i))
        i += 1
      }

    }
  }

  def withTemporaryDirectory(fn: Path => Unit) {
    val file = File.createTempFile("test", ".tmp")
    assert(file.delete())
    assert(file.mkdir())
    fn(file.toPath)
  }
}
