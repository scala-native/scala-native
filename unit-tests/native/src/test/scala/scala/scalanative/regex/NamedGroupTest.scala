package scala.scalanative
package regex

import scala.util.Random

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import TestUtils.*

import scala.collection.mutable

class NamedGroupTest {

  @Test def namedGroupIsStackSafe(): Unit = {
    val buf = new StringBuffer()
    val added = mutable.Set[String]()
    var i = 0
    def randomGroupName(): String = Random.alphanumeric.take(10).mkString("")
    while (i < 20000) {
      var name = randomGroupName()
      while (added.contains(name)) name = randomGroupName()

      buf.append("(?<" + name + ">test)")
      i += 1
    }
    Pattern.compile(buf.toString())
  }

  @Test def namedGroup(): Unit = {
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m.*

    assertTrue(find())
    assertTrue(group("S") == "Montreal, Canada")
    assertTrue(group("D") == "Lausanne, Switzerland")
  }

  @Test def startNameEndName(): Unit = {
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m.*

    assertTrue(find())
    assertTrue(start("S") == 5)
    assertTrue(end("S") == 21)

    assertTrue(start("D") == 25)
    assertTrue(end("D") == 46)
  }

  @Test def appendReplacementAppendTailWithGroupReplacementByName(): Unit = {
    val buf = new StringBuffer()
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m.*
    while (find()) {
      appendReplacement(buf, "such ${S}, wow ${D}")
    }
    appendTail(buf)

    val obtained = buf.toString
    val expected = "such Montreal, Canada, wow Lausanne, Switzerland"

    assertTrue(obtained == expected)
  }

  @Test def appendReplacementUnclosed(): Unit = {
    val buf = new StringBuffer()
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m.*
    find()
    assertThrows(
      "No match available",
      classOf[IllegalStateException],
      appendReplacement(buf, "such open ${S such closed ${D}")
    )
  }
}
