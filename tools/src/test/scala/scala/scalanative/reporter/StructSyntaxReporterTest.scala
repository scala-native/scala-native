package scala.scalanative.reporter

class StructSyntaxReporterTest extends NirErrorReporterSpec {

  it should "do not allow passing struct by value" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |import scala.scalanative.runtime.struct
         |
         |@extern object testC{
         |  @struct class Foo(n: CInt)
         |  def consumeFoo(foo: Foo): Unit = extern
         |}
         |
         |object test {
         |  def test(): Unit = {
         |    import testC._
         |    consumeFoo(new Foo(0))
         |  }
         |}""".stripMargin
    }("Passing struct by value to external functions is currently unsupported")
  }

}
