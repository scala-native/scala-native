package scala

import org.junit.Test
import org.junit.Assert.*

class EnumerationTest {

  object SimpleEnum extends Enumeration {
    val a, b, c = Value
  }

  object NamedEnum extends Enumeration {
    val a = Value("z")
    val b = Value("y")
    val c = Value("x")
  }

  object NumberedEnum extends Enumeration(5) {
    val Five, Six, Seven = Value
  }

  @Test def elementsInSimpleEnumAreCorrect(): Unit = {
    assertTrue(SimpleEnum.values.size == 3)

    assertTrue(SimpleEnum.a.id == 0)
    assertTrue(SimpleEnum.a.toString == "a")

    assertTrue(SimpleEnum.b.id == 1)
    assertTrue(SimpleEnum.b.toString == "b")

    assertTrue(SimpleEnum.c.id == 2)
    assertTrue(SimpleEnum.c.toString == "c")
  }

  @Test def elementsUseTheNamesTheyWereGiven(): Unit = {
    assertTrue(NamedEnum.values.size == 3)

    assertTrue(NamedEnum.a.id == 0)
    assertTrue(NamedEnum.a.toString == "z")

    assertTrue(NamedEnum.b.id == 1)
    assertTrue(NamedEnum.b.toString == "y")

    assertTrue(NamedEnum.c.id == 2)
    assertTrue(NamedEnum.c.toString == "x")
  }

  @Test def elementsUseTheNamesAndIdTheyWereGiven(): Unit = {
    assertTrue(NumberedEnum.values.size == 3)

    assertTrue(NumberedEnum.Five.id == 5)
    assertTrue(NumberedEnum.Five.toString == "Five")

    assertTrue(NumberedEnum.Six.id == 6)
    assertTrue(NumberedEnum.Six.toString == "Six")

    assertTrue(NumberedEnum.Seven.id == 7)
    assertTrue(NumberedEnum.Seven.toString == "Seven")
  }

}
