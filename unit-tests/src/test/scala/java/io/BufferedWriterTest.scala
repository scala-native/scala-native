package java.io

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class BufferedWriterTest {

  @Test def creatingBufferedWriterWithBufferSizeZeroThrowsException(): Unit = {
    val writer = new OutputStreamWriter(new ByteArrayOutputStream)
    assertThrows(classOf[IllegalArgumentException],
                 new BufferedWriter(writer, -1))
  }

  @Test def canWriteSmallChunksToBufferedWriter(): Unit = {
    val stream = new ByteArrayOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stream))
    val string = "Hello, world!"
    writer.write(string)
    writer.flush()
    assertTrue(stream.toString == string)
  }

  @Test def canWriteChunkLargerThanBufferSizeToBufferedWriter(): Unit = {
    val stream = new ByteArrayOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stream), 1)
    val string = "Hello, world!"
    writer.write(string)
    writer.flush()
    assertTrue(stream.toString == string)
  }

  @Test def closingTwiceIsHarmless(): Unit = {
    val stream = new ByteArrayOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(stream))
    writer.close()
    writer.close()
  }

}
