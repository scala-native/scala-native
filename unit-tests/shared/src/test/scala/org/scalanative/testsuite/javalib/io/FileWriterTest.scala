package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Test

class FileWriterTest {

  @Test def writeToNewFile(): Unit = {
    val tmpDir = System.getProperty("java.io.tmpdir")
    assertNotEquals("Not set sys tmp directory property", null, tmpDir)

    val path = s"$tmpDir/new.file"
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
