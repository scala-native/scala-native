package scala
package partest

object t10032Suite extends tests.Suite {
  var effects                    = List.empty[String]
  def println(str: String): Unit = effects = effects :+ str
  def clean(): Unit              = effects = Nil
  def assertEffects(effects: String*)(f: => Unit) = {
    this.effects = Nil
    f
    assert(this.effects == effects.toList)
  }

  def a1(): Unit = println("a1")
  def a2(): Unit = println("a2")
  def a3(): Unit = println("a3")

  def i1: Int = { println("i1"); 1 }
  def i2: Int = { println("i2"); 2 }
  def i3: Int = { println("i3"); 3 }

  def e1: Int = { println("e1"); throw new Exception() }

  def t1: Int = {
    println("t1")
    try {
      synchronized { return i1 }
    } finally {
      synchronized { a1() }
    }
  }

  def t2: Int = {
    println("t2")
    try {
      try { return i1 } finally { a1() }
    } finally {
      try { a2() } finally { a3() }
    }
  }

  def t3(i: => Int): Int = {
    println("t3")
    try {
      try { return i } finally { a1() }
    } catch {
      case _: Throwable =>
        try { i2 } finally { a2() } // no cleanup version
    } finally {
      a3()
    }
  }

  def t4(i: => Int): Int = {
    println("t4")
    try {
      return i
    } finally {
      return i2
    }
  }

  def t5(i: => Int): Int = {
    println("t5")
    try {
      try {
        try { return i } finally { a1() }
      } catch {
        case _: Throwable => i2
      }
    } finally {
      a3()
    }
  }

  def t6(i: => Int): Int = {
    println("t6")
    try {
      try { return i } finally { return i2 }
    } finally {
      return i3
    }
  }

  def t7(i: => Int): Int = {
    println("t7")
    try { i } catch {
      case _: Throwable =>
        return i2
    } finally {
      a1() // cleanup required, early return in handler
    }
  }

  def t8(i: => Int): Int = {
    println("t8")
    try {
      try { i } finally { // no cleanup version
        try { return i2 } finally { a1() } // cleanup version required
      }
    } finally { // cleanup version required
      a2()
    }
  }

  def t9(i: => Int): Int = {
    println("t9")
    try {
      return i
    } finally {
      try { return i2 } finally { a1() }
    }
  }

  def t10(i: => Int): Int = {
    println("t10")
    try {
      return i
    } finally {
      try { return i2 } finally { return i3 }
    }
  }

  // this changed semantics between 2.12.0 and 2.12.1, see https://github.com/scala/scala/pull/5509#issuecomment-259291609
  def t11(i: => Int): Int = {
    println("t11")
    try {
      try { return i } finally { return i2 }
    } finally {
      a1()
    }
  }

  test("t1") {
    assertEffects("t1", "i1", "a1") {
      assert(t1 == 1)
    }
  }

  test("t2") {
    assertEffects("t2", "i1", "a1", "a2", "a3") {
      assert(t2 == 1)
    }
  }

  test("t3.i1") {
    assertEffects("t3", "i1", "a1", "a3") {
      assert(t3(i1) == 1)
    }
  }

  test("t3.e1") {
    assertEffects("t3", "e1", "a1", "i2", "a2", "a3") {
      assert(t3(e1) == 2)
    }
  }

  test("t4.i1") {
    assertEffects("t4", "i1", "i2") {
      assert(t4(i1) == 2)
    }
  }

  test("t4.e1") {
    assertEffects("t4", "e1", "i2") {
      assert(t4(e1) == 2)
    }
  }

  test("t5.i1") {
    assertEffects("t5", "i1", "a1", "a3") {
      assert(t5(i1) == 1)
    }
  }

  test("t5.e1") {
    assertEffects("t5", "e1", "a1", "i2", "a3") {
      assert(t5(e1) == 2)
    }
  }

  test("t6.i1") {
    assertEffects("t6", "i1", "i2", "i3") {
      assert(t6(i1) == 3)
    }
  }

  test("t6.e1") {
    assertEffects("t6", "e1", "i2", "i3") {
      assert(t6(e1) == 3)
    }
  }

  test("t7.i1") {
    assertEffects("t7", "i1", "a1") {
      assert(t7(i1) == 1)
    }
  }

  test("t7.e1") {
    assertEffects("t7", "e1", "i2", "a1") {
      assert(t7(e1) == 2)
    }
  }

  test("t8.i1") {
    assertEffects("t8", "i1", "i2", "a1", "a2") {
      assert(t8(i1) == 2)
    }
  }

  test("t8.e1") {
    assertEffects("t8", "e1", "i2", "a1", "a2") {
      assert(t8(e1) == 2)
    }
  }

  test("t9.i1") {
    assertEffects("t9", "i1", "i2", "a1") {
      assert(t9(i1) == 2)
    }
  }

  test("t9.e1") {
    assertEffects("t9", "e1", "i2", "a1") {
      assert(t9(e1) == 2)
    }
  }

  test("t10.i1") {
    assertEffects("t10", "i1", "i2", "i3") {
      assert(t10(i1) == 3)
    }
  }

  test("t10.e1") {
    assertEffects("t10", "e1", "i2", "i3") {
      assert(t10(e1) == 3)
    }
  }

  test("t11.i1") {
    assertEffects("t11", "i1", "i2", "a1") {
      assert(t11(i1) == 2)
    }
  }

  test("t11.e1") {
    assertEffects("t11", "e1", "i2", "a1") {
      assert(t11(e1) == 2)
    }
  }
}
