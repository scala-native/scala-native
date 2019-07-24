package java.util

object DateSuite extends tests.Suite {
  // now : java.util.Date = Fri Mar 31 14:47:44 EDT 2017
  val nowUt    = 1490986064740L
  val beforeUt = 1490986059300L
  val afterUt  = 1490986090620L
  val now      = new Date(nowUt)
  val before   = new Date(beforeUt)
  val after    = new Date(afterUt)
  val now2     = new Date(nowUt)
  test("after") {
    assert(after.after(now))
  }

  test("before") {
    assert(before.before(now))
  }

  test("clone") {
    val clone = now.clone().asInstanceOf[Date]
    assert(clone.getTime equals now.getTime)
  }

  test("compareTo") {
    assert(now.compareTo(now2) == 0)
    assert(before.compareTo(now) == -1)
    assert(after.compareTo(now) == 1)
  }

  test("equals") {
    assert(now.equals(now2))
  }

  test("getTime") {
    assert(now.getTime == nowUt)
  }

  test("hashCode") {
    assert(now.hashCode == nowUt.hashCode())
  }

  test("setTime") {
    val nowBefore = new Date(nowUt)
    nowBefore.setTime(afterUt)
    assert(nowBefore equals after)
  }

  test("toString") {
    // val now : java.util.Date = Fri Mar 31 14:47:44 EDT 2017
    val result = now.toString
    val expected = "[A-Z][a-z]{2} [A-Z][a-z]{2} " +
      "\\d\\d \\d{2}:\\d{2}:\\d{2} [A-Z]{2,5} 20[1-3]\\d"

    assert(result.matches(expected),
           s"result: '${result}' does not match expected regex: '${expected}'")
  }

}
