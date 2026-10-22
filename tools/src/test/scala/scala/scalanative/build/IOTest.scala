package scala.scalanative.build

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

class IOTest {

  @Test def writeReplacesLongerContent(): Unit = {
    val dir = Files.createTempDirectory("scala-native-io-test")
    val file = dir.resolve("build-checksum")
    try {
      IO.write(file, "18176379366")
      IO.write(file, "1817637936")
      assertEquals(Some("1817637936"), IO.readFully(file))
    } finally IO.deleteRecursive(dir)
  }
}
