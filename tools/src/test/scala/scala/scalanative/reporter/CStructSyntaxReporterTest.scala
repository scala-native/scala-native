package scala.scalanative.reporter

class CStructSyntaxReporterTest extends NirErrorReporterSpec {

  it should "not allow passing struct to external func by value" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |@extern
         |object testC{
         |  type Foo = CStruct1[CInt]
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
    }("Passing struct by value to external functions is currently unsupported")
  }

  it should "allow passing struct to external func by ref" in {
    allows {
      s"""
         |import scala.scalanative.unsafe._
         |@extern
         |object testC{
         |  type Foo = CStruct1[CInt]
         |  def consumeFoo(foo: Ptr[Foo]): Unit = extern
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
