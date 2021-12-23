package scala.issues

object Macros:
  import scala.quoted.*

  inline def test(a: String): Seq[Int] = ${
    testCode('a)
  }
  private def testCode(s: Expr[String])(using Quotes): Expr[Seq[Int]] =
    import quotes.reflect.*
    Expr(List(1, 2, 3))
