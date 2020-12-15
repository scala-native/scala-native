package scala.scalanative.reporter

class CArraySyntaxReporterTest extends NirErrorReporterSpec {

  it should "do not allow passing fixed size array by value" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |@extern
         |object testC{
         |  type Foo = CArray[CInt, Nat._1]
         |  def consumeFoo(foo: Foo): Unit = extern
         |}
         |
         |object test {
         |  def test(): Unit = {
         |    import testC._
         |    val x = !stackalloc[Foo]
         |    consumeFoo(x)
         |  }
         |}""".stripMargin
    }("Passing fixed size array to extern function by value is currently unsupported")
  }

  it should "allow passing fixed size array by ref" in {
    allows {
      s"""
         |import scala.scalanative.unsafe._
         |@extern
         |object testC{
         |  type Foo = CArray[CInt, Nat._1]
         |  def consumeFoo(foo: Ptr[Foo]): Unit = extern
         |  
         |}
         |
         |object test {
         |  def test(): Unit = {
         |    import testC._
         |    val x = stackalloc[Foo]
         |    consumeFoo(x)
         |  }
         |}""".stripMargin
    }
  }
}
