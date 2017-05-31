package java.util

// Ported from Apache Harmony

object StringTokenizerSuite extends tests.Suite {

  test("Constructor(String, String)") {
    val st = new StringTokenizer("This:is:a:test:String", ":")
    assert(st.countTokens() == 5)
    assert(st.nextElement() == "This")
  }

  test("Constructor(String, String, Boolean)") {
    val st = new StringTokenizer("This:is:a:test:String", ":", true)
    st.nextElement()
    assert(st.countTokens() == 8)
    assert(st.nextElement() == ":")
  }

  test("countTokens()") {
    val st = new StringTokenizer("This is a test String")
    assert(st.countTokens() == 5)
  }

  test("hasMoreElements()") {
    val st = new StringTokenizer("This is a test String")
    st.nextElement()
    assert(st.hasMoreElements())
    st.nextElement()
    st.nextElement()
    st.nextElement()
    st.nextElement()
    assert(!st.hasMoreElements())
  }

  test("hasMoreTokens()") {
    val st      = new StringTokenizer("This is a test String")
    var counter = 0
    while (counter < 5) {
      assert(st.hasMoreTokens())
      st.nextToken()
      counter += 1
    }
    assert(!st.hasMoreTokens())
  }

  test("nextElement()") {
    val st = new StringTokenizer("This is a test String")
    assert(st.nextElement() == "This")
    assert(st.nextElement() == "is")
    assert(st.nextElement() == "a")
    assert(st.nextElement() == "test")
    assert(st.nextElement() == "String")

    assertThrows[NoSuchElementException] {
      st.nextElement()
    }
  }

  test("nextToken()") {
    val st = new StringTokenizer("This is a test String")
    assert(st.nextToken() == "This")
    assert(st.nextToken() == "is")
    assert(st.nextToken() == "a")
    assert(st.nextToken() == "test")
    assert(st.nextToken() == "String")

    assertThrows[NoSuchElementException] {
      st.nextToken()
    }
  }

  test("nextToken(String)") {
    val st = new StringTokenizer("This is a test String")
    assert(st.nextToken(" ") == "This")
    assert(st.nextToken("tr") == " is a ")
    assert(st.nextToken() == "es")
  }

  test("hasMoreElements_NPE") {
    val st = new StringTokenizer(new String(), null, true)
    assertThrows[NullPointerException] {
      st.hasMoreElements()
    }

    val st2 = new StringTokenizer(new String(), null)
    assertThrows[NullPointerException] {
      st2.hasMoreElements()
    }
  }

  test("hasMoreTokens_NPE") {
    val st = new StringTokenizer(new String, null, true)
    assertThrows[NullPointerException] {
      st.hasMoreTokens()
    }

    val st2 = new StringTokenizer(new String, null)
    assertThrows[NullPointerException] {
      st2.hasMoreTokens()
    }
  }

  test("nextToken_NPE") {
    val st = new StringTokenizer(new String, null, true)
    assertThrows[NullPointerException] {
      st.nextToken()
    }

    val st2 = new StringTokenizer(new String, null)
    assertThrows[NullPointerException] {
      st2.nextToken()
    }
  }

}
