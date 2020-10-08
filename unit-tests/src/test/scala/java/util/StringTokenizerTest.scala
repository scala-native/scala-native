package java.util

// Ported from Apache Harmony

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.junit.utils.AssertThrows._

class StringTokenizerTest {

  @Test def constructorStringString(): Unit = {
    val st = new StringTokenizer("This:is:a:test:String", ":")
    assert(st.countTokens() == 5)
    assert(st.nextElement() == "This")
  }

  @Test def constructorStringStringBoolean(): Unit = {
    val st = new StringTokenizer("This:is:a:test:String", ":", true)
    st.nextElement()
    assert(st.countTokens() == 8)
    assert(st.nextElement() == ":")
  }

  @Test def countTokens(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assert(st.countTokens() == 5)
  }

  @Test def hasMoreElements(): Unit = {
    val st = new StringTokenizer("This is a test String")
    st.nextElement()
    assert(st.hasMoreElements())
    st.nextElement()
    st.nextElement()
    st.nextElement()
    st.nextElement()
    assert(!st.hasMoreElements())
  }

  @Test def hasMoreTokens(): Unit = {
    val st      = new StringTokenizer("This is a test String")
    var counter = 0
    while (counter < 5) {
      assert(st.hasMoreTokens())
      st.nextToken()
      counter += 1
    }
    assert(!st.hasMoreTokens())
  }

  @Test def nextElement(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assert(st.nextElement() == "This")
    assert(st.nextElement() == "is")
    assert(st.nextElement() == "a")
    assert(st.nextElement() == "test")
    assert(st.nextElement() == "String")

    assertThrows(classOf[NoSuchElementException], st.nextElement())
  }

  @Test def nextToken(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assert(st.nextToken() == "This")
    assert(st.nextToken() == "is")
    assert(st.nextToken() == "a")
    assert(st.nextToken() == "test")
    assert(st.nextToken() == "String")

    assertThrows(classOf[NoSuchElementException], st.nextToken())
  }

  @Test def nextTokenString(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assert(st.nextToken(" ") == "This")
    assert(st.nextToken("tr") == " is a ")
    assert(st.nextToken() == "es")
  }

  @Test def hasMoreElementsNPE(): Unit = {
    val st = new StringTokenizer(new String(), null, true)
    assertThrows(classOf[NullPointerException], st.hasMoreElements())

    val st2 = new StringTokenizer(new String(), null)
    assertThrows(classOf[NullPointerException], st2.hasMoreElements())
  }

  @Test def hasMoreTokensNPE(): Unit = {
    val st = new StringTokenizer(new String, null, true)
    assertThrows(classOf[NullPointerException], st.hasMoreTokens())

    val st2 = new StringTokenizer(new String, null)
    assertThrows(classOf[NullPointerException], st2.hasMoreTokens())
  }

  @Test def nextTokenNPE(): Unit = {
    val st = new StringTokenizer(new String, null, true)
    assertThrows(classOf[NullPointerException], st.nextToken())

    val st2 = new StringTokenizer(new String, null)
    assertThrows(classOf[NullPointerException], st2.nextToken())
  }

}
