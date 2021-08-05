package javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._

class FileWriterTest {

  @Test def writeToNewFile(): Unit = {
    val path = "/tmp/new.file"
    val toWrite = "something"

    val fw = new FileWriter(path)
    fw.write(toWrite)
    fw.close()

    val buffer = new Array[Char](20)
    val fr = new FileReader(path)
    val charsRead = fr.read(buffer)
    assertTrue(charsRead == toWrite.length)
    assertTrue(buffer.take(charsRead).mkString == toWrite)
    fr.close()
  }
}
