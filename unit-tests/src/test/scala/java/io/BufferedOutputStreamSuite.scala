package java.io

// Ported from Apache Harmony
object BufferedOutputStreamSuite extends tests.Suite {

  val fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n"

  test("Constructor(OutputStream)") {
    val baos = new java.io.ByteArrayOutputStream()
    val os   = new java.io.BufferedOutputStream(baos)
    os.write(fileString.getBytes(), 0, 500)
  }

  test("Constructor(OutputStream, Int)") {
    val baos = new java.io.ByteArrayOutputStream()
    val os   = new java.io.BufferedOutputStream(baos, 1024)
    os.write(fileString.getBytes(), 0, 500)
  }

  test("Size must be > 0") {
    assertThrows[IllegalArgumentException] {
      new BufferedOutputStream(null, 0)
    }

    assertThrows[IllegalArgumentException] {
      new BufferedOutputStream(null, -1)
    }
  }

  test("flush()") {
    val baos = new ByteArrayOutputStream()
    val os   = new java.io.BufferedOutputStream(baos, 600)
    os.write(fileString.getBytes(), 0, 500)
    os.flush()
    assertEquals(500, baos.size())
  }

  private class MockOutputStream(size: Int) extends OutputStream {
    val written: Array[Byte] = new Array[Byte](size)
    var count: Int           = 0

    def write(b: Int): Unit = {
      written(count) = b.toByte
      count += 1
    }

    def getWritten(): String = {
      new String(written, 0, count)
    }
  }

  test("write(Array[Byte], Int, Int)") {
    val baos = new ByteArrayOutputStream()
    val os   = new BufferedOutputStream(baos, 512)
    os.write(fileString.getBytes(), 0, 500)
    var bais = new ByteArrayInputStream(baos.toByteArray())
    assertEquals(0, bais.available())
    os.flush()
    bais = new ByteArrayInputStream(baos.toByteArray())
    assertEquals(500, bais.available())
    os.write(fileString.getBytes(), 500, 513)
    bais = new ByteArrayInputStream(baos.toByteArray())
    assert(bais.available() >= 1000)
    val wbytes = new Array[Byte](1013)
    bais.read(wbytes, 0, 1013)
    assertEquals(new String(wbytes, 0, wbytes.length),
                 fileString.substring(0, 1013))

    // regression test for HARMONY-4177
    var mos = new MockOutputStream(5)
    var bos = new BufferedOutputStream(mos, 3)
    bos.write("a".getBytes())
    bos.write("bcde".getBytes())
    assertEquals("abcde",
                 mos
                   .getWritten())
    mos = new MockOutputStream(4)
    bos = new BufferedOutputStream(mos, 3)
    bos.write("ab".getBytes())
    bos.write("cd".getBytes())
    assertEquals("ab", mos.getWritten())
  }

  test("write(Array[Byte], Int, Int)") {
    val bos                        = new BufferedOutputStream(new ByteArrayOutputStream())
    val nullByteArray: Array[Byte] = null
    val byteArray                  = new Array[Byte](10)

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, -1, -1)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, -1, 0)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, -1, 1)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, 0, -1)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, 0, byteArray.length + 1)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, 1, byteArray.length)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, -1, byteArray.length)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, byteArray.length, -1)
    }
    bos.write(byteArray, byteArray.length, 0)
    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, byteArray.length, 1)
    }

    bos.write(byteArray, 0, 0)
    bos.write(byteArray, 0, 1)
    bos.write(byteArray, 1, byteArray.length - 1)
    bos.write(byteArray, 0, byteArray.length)

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, 1, -1)
    }

    bos.write(byteArray, 1, 0)
    bos.write(byteArray, 1, 1)

    bos.write(byteArray, byteArray.length, 0)

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, byteArray.length + 1, 0)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, byteArray.length + 1, 1)
    }

    bos.close()

    assertThrows[ArrayIndexOutOfBoundsException] {
      bos.write(byteArray, -1, -1)
    }
  }

  test("write(Int)") {
    val baos = new java.io.ByteArrayOutputStream()
    val os   = new java.io.BufferedOutputStream(baos)
    os.write('t')
    var bais = new java.io.ByteArrayInputStream(baos.toByteArray())
    assertEquals(0, bais.available())
    os.flush()
    bais = new java.io.ByteArrayInputStream(baos.toByteArray())
    assertEquals(1, bais.available())
    val wbytes = new Array[Byte](1)
    bais.read(wbytes, 0, 1)
    assertEquals('t', wbytes(0))
  }

  test("close()") {
    val buffos = new BufferedOutputStream(new ByteArrayOutputStream())
    buffos.write(new Array[Byte](0))
    val buffer = "1234567890".getBytes()

    buffos.write(Int.MinValue)
    buffos.write(Int.MaxValue)
    buffos.write(buffer, 0, 10)
    buffos.flush()

    buffos.close()
  }

  test("Write scenario 1") {
    val byteArrayos                       = new ByteArrayOutputStream()
    var byteArrayis: ByteArrayInputStream = null
    val buffer                            = "1234567890".getBytes("UTF-8")

    val buffos = new BufferedOutputStream(byteArrayos, 10)
    buffos.write(buffer, 0, 10)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(10, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(10,
                 byteArrayis
                   .available())
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }

    buffos.write(buffer, 0, 10)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(20, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(20,
                 byteArrayis
                   .available())
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }

    buffos.write(buffer, 0, 10)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(30, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(30,
                 byteArrayis
                   .available())
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
  }

  test("Write scenario 2") {
    val byteArrayos                       = new ByteArrayOutputStream()
    var byteArrayis: ByteArrayInputStream = null
    val buffer                            = "1234567890".getBytes("UTF-8")

    val buffos = new BufferedOutputStream(byteArrayos, 20)
    buffos.write(buffer, 0, 10)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(0, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(10,
                 byteArrayis
                   .available())
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }

    val buffer2 = Array[Byte]('a', 'b', 'c', 'd')
    buffos.write(buffer2, 0, 4)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(10, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(14,
                 byteArrayis
                   .available())
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
    (0 until 4).foreach { i =>
      assertEquals(buffer2(i), byteArrayis.read())
    }

    val buffer3 = Array[Byte]('e', 'f', 'g', 'h', 'i')
    buffos.write(buffer3, 0, 5)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(14, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(19,
                 byteArrayis
                   .available())
    (0 until 10).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
    (0 until 4).foreach { i =>
      assertEquals(buffer2(i), byteArrayis.read())
    }
    (0 until 5).foreach { i =>
      assertEquals(buffer3(i), byteArrayis.read())
    }

    buffos.write(Array[Byte]('j', 'k'))
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(19, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(21,
                 byteArrayis
                   .available())

    buffos.close()
  }

  test("Write scenario 3") {
    val byteArrayos                       = new ByteArrayOutputStream()
    var byteArrayis: ByteArrayInputStream = null
    val buffer                            = "1234567890".getBytes("UTF-8")

    val buffos = new BufferedOutputStream(byteArrayos, 5)
    buffos.write(buffer, 0, 4)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(0, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(4,
                 byteArrayis
                   .available())
    (0 until 4).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }

    buffos.write(buffer, 0, 5)
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(9, byteArrayis.available())
    buffos.flush()
    byteArrayis = new ByteArrayInputStream(byteArrayos.toByteArray())
    assertEquals(9,
                 byteArrayis
                   .available())
    (0 until 4).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
    (0 until 5).foreach { i =>
      assertEquals(buffer(i), byteArrayis.read())
    }
  }

  // Regression test for flush on closed stream
  test("flush on closed stream") {
    val bos =
      new BufferedOutputStream(new ByteArrayOutputStream())
    bos.close()
    bos.flush() // RI does not throw exception
  }

  test("creating a buffer of negative size throws IllegalArgumentException") {
    assertThrows[IllegalArgumentException] {
      val out = new BufferedOutputStream(new ByteArrayOutputStream(), -1)
    }
  }

  test("write to closed Buffer throws IOException") {

    val out = new BufferedOutputStream(new ByteArrayOutputStream())

    out.close()

    assertThrows[java.io.IOException](out.write(1))

  }

  test("simple write") {

    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    out.write(0)
    out.write(1)
    out.write(2)

    out.flush()

    val ans = arrayOut.toByteArray

    assert(ans(0) == 0 && ans(1) == 1 && ans(2) == 2)
  }

  test("write without flush does nothing") {
    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    out.write(0)
    out.write(1)
    out.write(2)

    assert(arrayOut.toByteArray.isEmpty)
  }

  test("simple write Array") {

    val array = List(0, 1, 2).map(_.toByte).toArray[Byte]

    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    out.write(array, 0, 3)

    out.flush()

    val ans = arrayOut.toByteArray
    assert(ans(0) == 0 && ans(1) == 1 && ans(2) == 2)

  }

  test("write array with bad index or length throw exceptions") {

    val array = List(0, 1, 2).map(_.toByte).toArray[Byte]

    val arrayOut = new ByteArrayOutputStream()

    val out = new BufferedOutputStream(arrayOut)

    assertThrows[ArrayIndexOutOfBoundsException] {
      out.write(array, 0, 4)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      out.write(array, 4, 3)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      out.write(array, -1, 3)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      out.write(array, 4, -1)
    }

  }

}
