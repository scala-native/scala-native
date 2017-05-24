package java.nio.channels

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}
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
      val f     = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      val src   = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f,
                                     StandardOpenOption.WRITE,
                                     StandardOpenOption.CREATE)
      while (src.remaining() > 0) channel.write(src)

      val in = Files.newInputStream(f)
      var i  = 0
      while (i < bytes.length) {
        assert(in.read() == bytes(i))
        i += 1
      }

    }
  }

  test("A FileChannel can overwrite a file") {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("file")
      Files.write(f, "hello, world".getBytes("UTF-8"))

      val bytes = "goodbye".getBytes("UTF-8")
      val src   = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f,
                                     StandardOpenOption.WRITE,
                                     StandardOpenOption.CREATE)
      while (src.remaining() > 0) channel.write(src)

      val in = Files.newInputStream(f)
      var i  = 0
      while (i < bytes.length) {
        assert(in.read() == bytes(i))
        i += 1
      }
    }
  }

  test("A file channel writes at the beginning, unless otherwise specified") {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "abcdefgh".getBytes("UTF-8"))
      val lines = Files.readAllLines(f)
      assert(lines.size() == 1)
      assert(lines.get(0) == "abcdefgh")

      val c   = FileChannel.open(f, StandardOpenOption.WRITE)
      val src = ByteBuffer.wrap("xyz".getBytes("UTF-8"))
      while (src.remaining() > 0) c.write(src)

      val newLines = Files.readAllLines(f)
      assert(newLines.size() == 1)
      assert(newLines.get(0) == "xyzdefgh")
    }
  }

  test("Cannot combine APPEND and TRUNCATE_EXISTING") {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      assertThrows[IllegalArgumentException] {
        FileChannel.open(f,
                         StandardOpenOption.APPEND,
                         StandardOpenOption.TRUNCATE_EXISTING)
      }
    }
  }

  test("Cannot combine APPEND and READ") {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      assertThrows[IllegalArgumentException] {
        FileChannel.open(f, StandardOpenOption.APPEND, StandardOpenOption.READ)
      }
    }
  }

  test("Can write to a channel with APPEND") {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "hello, ".getBytes("UTF-8"))

      val lines = Files.readAllLines(f)
      assert(lines.size() == 1)
      assert(lines.get(0) == "hello, ")

      val bytes   = "world".getBytes("UTF-8")
      val src     = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f, StandardOpenOption.APPEND)
      while (src.remaining() > 0) channel.write(src)

      val newLines = Files.readAllLines(f)
      assert(newLines.size() == 1)
      assert(newLines.get(0) == "hello, world")
    }
  }

  def withTemporaryDirectory(fn: Path => Unit) {
    val file = File.createTempFile("test", ".tmp")
    assert(file.delete())
    assert(file.mkdir())
    fn(file.toPath)
  }
}
