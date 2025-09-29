package org.scalanative.testsuite.javalib.util

import java.util._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringTokenizerTest {

  @Test def constructorStringString(): Unit = {
    val st = new StringTokenizer("This:is:a:test:String", ":")
    assertTrue(st.countTokens() == 5)
    assertTrue(st.nextElement() == "This")
  }

  @Test def constructorStringStringBoolean(): Unit = {
    val st = new StringTokenizer("This:is:a:test:String", ":", true)
    st.nextElement()
    assertTrue(st.countTokens() == 8)
    assertTrue(st.nextElement() == ":")
  }

  @Test def countTokens(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assertTrue(st.countTokens() == 5)
  }

  @Test def hasMoreElements(): Unit = {
    val st = new StringTokenizer("This is a test String")
    st.nextElement()
    assertTrue(st.hasMoreElements())
    st.nextElement()
    st.nextElement()
    st.nextElement()
    st.nextElement()
    assertFalse(st.hasMoreElements())
  }

  @Test def hasMoreTokens(): Unit = {
    val st = new StringTokenizer("This is a test String")
    var counter = 0
    while (counter < 5) {
      assertTrue(st.hasMoreTokens())
      st.nextToken()
      counter += 1
    }
    assertFalse(st.hasMoreTokens())
  }

  @Test def nextElement(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assertTrue(st.nextElement() == "This")
    assertTrue(st.nextElement() == "is")
    assertTrue(st.nextElement() == "a")
    assertTrue(st.nextElement() == "test")
    assertTrue(st.nextElement() == "String")

    assertThrows(classOf[NoSuchElementException], st.nextElement())
  }

  @Test def nextToken(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assertTrue(st.nextToken() == "This")
    assertTrue(st.nextToken() == "is")
    assertTrue(st.nextToken() == "a")
    assertTrue(st.nextToken() == "test")
    assertTrue(st.nextToken() == "String")

    assertThrows(classOf[NoSuchElementException], st.nextToken())
  }

  @Test def nextTokenString(): Unit = {
    val st = new StringTokenizer("This is a test String")
    assertTrue(st.nextToken(" ") == "This")
    assertTrue(st.nextToken("tr") == " is a ")
    assertTrue(st.nextToken() == "es")
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
