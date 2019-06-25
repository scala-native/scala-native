package scala

object EnumerationSuite extends tests.Suite {

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

  test("Elements in a simple enum are correct") {
    assert(SimpleEnum.values.size == 3)

    assert(SimpleEnum.a.id == 0)
    assert(SimpleEnum.a.toString == "a")

    assert(SimpleEnum.b.id == 1)
    assert(SimpleEnum.b.toString == "b")

    assert(SimpleEnum.c.id == 2)
    assert(SimpleEnum.c.toString == "c")
  }

  test("Elements use the names they were given") {
    assert(NamedEnum.values.size == 3)

    assert(NamedEnum.a.id == 0)
    assert(NamedEnum.a.toString == "z")

    assert(NamedEnum.b.id == 1)
    assert(NamedEnum.b.toString == "y")

    assert(NamedEnum.c.id == 2)
    assert(NamedEnum.c.toString == "x")
  }

  test("Elements use the names and IDs they were given") {
    assert(NumberedEnum.values.size == 3)

    assert(NumberedEnum.Five.id == 5)
    assert(NumberedEnum.Five.toString == "Five")

    assert(NumberedEnum.Six.id == 6)
    assert(NumberedEnum.Six.toString == "Six")

    assert(NumberedEnum.Seven.id == 7)
    assert(NumberedEnum.Seven.toString == "Seven")
  }

}
