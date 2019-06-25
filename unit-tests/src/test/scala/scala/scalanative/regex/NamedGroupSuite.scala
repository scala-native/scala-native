package scala.scalanative
package regex

import TestUtils._
import scala.util.Random

object NamedGroupSuite extends tests.Suite {

  test("named group is stack safe") {
    val buf                       = new StringBuffer()
    var i                         = 0
    def randomGroupName(): String = Random.alphanumeric.take(5).mkString("")
    while (i < 20000) {
      buf.append("(?<" + randomGroupName + ">test)")
      i += 1
    }
    Pattern.compile(buf.toString())
  }

  test("named group") {
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assert(group("S") == "Montreal, Canada")
    assert(group("D") == "Lausanne, Switzerland")
  }

  test("start(name)/end(name)") {
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assert(start("S") == 5)
    assert(end("S") == 21)

    assert(start("D") == 25)
    assert(end("D") == 46)
  }

  test("appendReplacement/appendTail with group replacement by name") {
    val buf = new StringBuffer()
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._
    while (find()) {
      appendReplacement(buf, "such ${S}, wow ${D}")
    }
    appendTail(buf)

    val obtained = buf.toString
    val expected = "such Montreal, Canada, wow Lausanne, Switzerland"

    assert(obtained == expected)
  }

  test("appendReplacement unclosed }") {
    val buf = new StringBuffer()
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._
    find()
    assertThrowsAnd[IllegalArgumentException](
      appendReplacement(buf, "such open ${S such closed ${D}"))(
      _.getMessage == "named capturing group is missing trailing '}'"
    )

  }
}
