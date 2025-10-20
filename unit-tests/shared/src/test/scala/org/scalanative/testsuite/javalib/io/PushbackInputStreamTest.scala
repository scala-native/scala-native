// Ported from Apache Harmony
package org.scalanative.testsuite.javalib.io

import java.io.*

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class PushbackInputStreamTest {

  @Test def reset(): Unit = {
    val pb =
      new PushbackInputStream(new ByteArrayInputStream(Array[Byte](0)), 2)
    assertThrows(classOf[IOException], pb.reset())
  }

  @Test def mark(): Unit = {
    val pb =
      new PushbackInputStream(new ByteArrayInputStream(Array[Byte](0)), 2)
    pb.mark(Int.MaxValue)
    pb.mark(0)
    pb.mark(-1)
    pb.mark(Int.MinValue)
  }

  @Test def constructorInputStream(): Unit = {
    val str = new PushbackInputStream(null)
    assertThrows(classOf[IOException], str.read())

    val pis =
      new PushbackInputStream(new ByteArrayInputStream("Hello".getBytes()))
    assertThrows(classOf[IOException], pis.unread("He".getBytes()))
  }

  @Test def constructorInputStreamInt(): Unit = {
    val pis =
      new PushbackInputStream(new ByteArrayInputStream("Hello".getBytes()))
    assertThrows(classOf[IOException], pis.unread("Hellos".getBytes()))

    val str = new PushbackInputStream(null, 1)
    assertThrows(classOf[IOException], str.read())
  }

  @Test def available(): Unit = {
    val pis = getPIS()
    assertTrue(pis.available() == fileString.getBytes().length)
  }

  @Test def read(): Unit = {
    val pis = getPIS()
    assertTrue(pis.read() == fileString.getBytes("UTF-8")(0))
  }

  @Test def readArrayByteIntInt(): Unit = {
    val pis = getPIS()
    val buf = new Array[Byte](100)
    pis.read(buf, 0, buf.length)
    assertTrue(new String(buf, "UTF-8") == fileString.substring(0, 100))
  }

  @Test def skipLong(): Unit = {
    val pis = getPIS()

    val buf = new Array[Byte](50)
    pis.skip(50)
    pis.read(buf, 0, buf.length)
    assertTrue(new String(buf, "UTF-8") == fileString.substring(50, 100))
    pis.unread(buf)

    pis.skip(25)
    val buf2 = new Array[Byte](25)
    pis.read(buf2, 0, buf2.length)
    assertTrue(new String(buf2, "UTF-8") == fileString.substring(75, 100))
  }

  @Test def unreadArrayByte(): Unit = {
    val pis = getPIS()

    val buf = new Array[Byte](100)
    pis.read(buf, 0, buf.length)
    assertTrue(new String(buf, "UTF-8") == fileString.substring(0, 100))
    pis.unread(buf)
    pis.read(buf, 0, 50)
    assertTrue(new String(buf, 0, 50, "UTF-8") == fileString.substring(0, 50))
  }

  @Test def unreadArrayByteIntInt(): Unit = {
    val pis = getPIS()

    val buf = new Array[Byte](100)
    pis.read(buf, 0, buf.length)
    assertTrue(new String(buf, "UTF-8") == fileString.substring(0, 100))
    pis.unread(buf, 50, 50)
    pis.read(buf, 0, 50)
    assertTrue(new String(buf, 0, 50, "UTF-8") == fileString.substring(50, 100))

    val pb =
      new PushbackInputStream(new ByteArrayInputStream(Array[Byte](0)), 2)
    assertThrows(classOf[IOException], pb.unread(new Array[Byte](1), 0, 5))
  }

  @Test def unreadInt(): Unit = {
    val pis = getPIS()

    val x = pis.read()
    assertTrue(x == fileString.getBytes("UTF-8")(0))
    pis.unread(x)
    assertTrue(pis.read() == x)
  }

  private def getPIS(): PushbackInputStream =
    new PushbackInputStream(
      new ByteArrayInputStream(fileString.getBytes("UTF-8")),
      65535
    )

  private val fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

}
