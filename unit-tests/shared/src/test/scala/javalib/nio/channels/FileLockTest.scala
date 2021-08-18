/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Ported from apache harmony
package javalib.nio.channels

import java.io.{File, FileOutputStream, IOException, RandomAccessFile}
import java.nio.channels.{ClosedChannelException, FileChannel, FileLock}

import org.junit.{Test, Before, After}
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class FileLockTest {

  private var readWriteChannel: FileChannel = null
  private var mockLock: MockFileLock = null

  class MockFileLock(
      channel: FileChannel,
      position: Long,
      size: Long,
      shared: Boolean
  ) extends FileLock(channel, position, size, shared) {

    var valid = true

    def isValid(): Boolean = {
      return valid
    }

    def release() = {
      valid = false
    }
  }

  @Before def setUp(): Unit = {
    val tempFile = File.createTempFile("testing", "tmp")
    tempFile.deleteOnExit()
    val randomAccessFile = new RandomAccessFile(tempFile, "rw")
    readWriteChannel = randomAccessFile.getChannel()
    mockLock = new MockFileLock(readWriteChannel, 10, 100, false)
  }

  @After def tearDown(): Unit = {
    if (readWriteChannel != null) {
      readWriteChannel.close()
    }
  }

  /** @tests
   *    java.nio.channels.FileLock#FileLock(FileChannel, long, long, boolean)
   */
  @Test def test_Constructor_Ljava_nio_channels_FileChannelJJZ(): Unit = {
    val fileLock1 = new MockFileLock(null, 0, 0, false)
    assertNull(fileLock1.channel())
    assertThrows(
      classOf[IllegalArgumentException],
      new MockFileLock(readWriteChannel, -1, 0, false)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      new MockFileLock(readWriteChannel, 0, -1, false)
    )
  }

  /** @tests
   *    java.nio.channels.FileLock#channel()
   */
  @Test def test_channel(): Unit = {
    assertSame(readWriteChannel, mockLock.channel())
    val lock = new MockFileLock(null, 0, 10, true)
    assertNull(lock.channel())
  }

  /** @tests
   *    java.nio.channels.FileLock#position()
   */
  @Test def test_position(): Unit = {
    val fileLock1 = new MockFileLock(readWriteChannel, 20, 100, true)
    assertEquals(20, fileLock1.position())

    val position = Integer.MAX_VALUE.toLong + 1
    val fileLock2 = new MockFileLock(readWriteChannel, position, 100, true)
    assertEquals(position, fileLock2.position())
  }

  /** @tests
   *    java.nio.channels.FileLock#size()
   */
  @Test def test_size(): Unit = {
    val fileLock1 = new MockFileLock(readWriteChannel, 20, 100, true)
    assertEquals(100, fileLock1.size())

    val position = 0x0fffffffffffffffL
    val size = Integer.MAX_VALUE.toLong + 1
    val fileLock2 = new MockFileLock(readWriteChannel, position, size, true)
    assertEquals(size, fileLock2.size())
  }

  /** @tests
   *    java.nio.channels.FileLock#isShared()
   */
  @Test def test_isShared(): Unit = {
    assertFalse(mockLock.isShared())
    val lock = new MockFileLock(null, 0, 10, true)
    assertTrue(lock.isShared())
  }

  /** @tests
   *    java.nio.channels.FileLock#overlaps(long, long)
   */
  @Test def test_overlaps_JJ(): Unit = {
    assertTrue("mockLock.overlaps(0, 11)", mockLock.overlaps(0, 11))
    assertFalse("mockLock.overlaps(0, 10)", mockLock.overlaps(0, 10))
    assertTrue("mockLock.overlaps(100, 110)", mockLock.overlaps(100, 110))
    assertTrue("mockLock.overlaps(99, 110)", mockLock.overlaps(99, 110))
    assertFalse("mockLock.overlaps(-1, 10)", mockLock.overlaps(-1, 10))
  }

  /** @tests
   *    java.nio.channels.FileLock#isValid()
   */
  @Test def test_isValid(): Unit = {
    val fileLock = readWriteChannel.lock()
    assertTrue(fileLock.isValid())
    fileLock.release()
    assertFalse(fileLock.isValid())
  }

  /** @tests
   *    java.nio.channels.FileLock#release()
   */
//   @Test def test_release(): Unit = {
//     val file = File.createTempFile("test", "tmp");
//     file.deleteOnExit();
//     val fout = new FileOutputStream(file);
//     val fileChannel = fout.getChannel();
//     val fileLock = fileChannel.lock();
//     fileChannel.close();
//     assertThrows(classOf[ClosedChannelException], fileLock.release())

//     // release after release
//     val fout2 = new FileOutputStream(file);
//     val fileChannel2 = fout.getChannel();
//     val fileLock2 = fileChannel.lock();
//     fileLock2.release();
//     fileChannel2.close();
//     assertThrows(classOf[ClosedChannelException], fileLock2.release())
//   }
}
