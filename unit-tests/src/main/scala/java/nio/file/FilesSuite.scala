package java.nio.file

import java.io.{ByteArrayInputStream, File, FileInputStream}

object FilesSuite extends tests.Suite {

  test("Files.copy can copy to a non-existing file") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(!targetFile.exists || targetFile.delete())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assert(Files.copy(in, target) == 3)
    assert(targetFile.exists())
    assert(in.read == -1)

    val fromFile = new FileInputStream(targetFile)
    assert(fromFile.read() == 1)
    assert(fromFile.read() == 2)
    assert(fromFile.read() == 3)
    assert(fromFile.read() == -1)
  }

}
