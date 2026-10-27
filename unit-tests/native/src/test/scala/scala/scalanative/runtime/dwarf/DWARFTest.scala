package scala.scalanative.runtime.dwarf

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.unsigned._

class DWARFTest {
  @Test def skipsUnsupportedDwarf5Unit(): Unit = {
    // DWARF32 v5 header, followed by a payload which must not be parsed.
    val bytes = Array[Byte](9, 0, 0, 0, 5, 0, 1, 8, 0, 0, 0, 0, 127)
    val path = Files.createTempFile("unsupported-dwarf5", ".bin")
    path.toFile.deleteOnExit()
    Files.write(path, bytes)
    implicit val file: BinaryFile = new BinaryFile(path.toFile)

    assertEquals(
      None,
      DWARF.DIE.parse(
        DWARF.Section(0.toUInt, bytes.length.toLong),
        DWARF.Section(0.toUInt, 0L)
      )
    )
    assertEquals(bytes.length.toLong, file.position())
  }
}
