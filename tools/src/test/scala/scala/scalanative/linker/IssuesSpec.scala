package scala.scalanative.linker

import scala.scalanative.checker.Check
import scala.scalanative.LinkerSpec

import org.scalatest.matchers.should._
import scala.scalanative.nir._

class IssuesSpec extends LinkerSpec with Matchers {
  private val mainClass = "Test"
  private val sourceFile = "Test.scala"

  private def testLinked(source: String, mainClass: String = mainClass)(
      fn: Result => Unit
  ): Unit =
    link(mainClass, sources = Map("Test.scala" -> source)) {
      case (_, result) => fn(result)
    }

  private def checkNoLinkageErrors(
      source: String,
      mainClass: String = mainClass
  ) =
    testLinked(source.stripMargin, mainClass) { result =>
      val erros = Check(result)
      erros shouldBe empty
    }

  "Issue #2790" should "link main classes using encoded characters" in {
    // All encoded character and an example of unciode encode character ';'
    val packageName = "foo.`b~a-r`.`b;a;z`"
    val mainClass = raw"Test-native~=<>!#%^&|*/+-:'?@;sc"
    val fqcn = s"$packageName.$mainClass".replace("`", "")
    checkNoLinkageErrors(
      mainClass = fqcn,
      source = s"""package $packageName
      |object `$mainClass`{ 
      |  def main(args: Array[String]) = () 
      |}
      |""".stripMargin
    )
  }

  "Issue #2880" should "handle lambas correctly" in checkNoLinkageErrors {
    """
    |object Test {
    |  trait ContextCodec[In, Out] {
    |    def decode(in: In, shouldFailFast: Boolean): Out
    |  }
    |
    |  def lift[In, Out](f: In => Out): ContextCodec[In, Out] =
    |    (in, shouldFailFast) => f(in)
    |
    |  def main(args: Array[String]): Unit = {
    |    lift[Any, Any](_ => ???).decode("foo", false)
    |  }
    |}
    |"""
  }

  "Extern traits" should "have only primitive type in type signature" in {
    testLinked(s"""
      |import scala.scalanative.unsafe._
      |import scala.scalanative.unsigned._
      |
      |@extern trait string {
      |  def memset(dest: Ptr[Byte], ch: Int, count: ULong): Ptr[Byte] = extern
      |}
      |@extern object string extends string
      |
      |object Test {
      |  def main(args: Array[String]): Unit = {
      |     val privilegeSetLength = stackalloc[ULong]()
      |     val privilegeSet: Ptr[Byte] = stackalloc[Byte](!privilegeSetLength)
      |     
      |     // real case
      |     string.memset(privilegeSet, 0, !privilegeSetLength)
      |     
      |     // possible case
      |     def str: string = ??? 
      |     str.memset(privilegeSet, 0, !privilegeSetLength)
      |  }
      |}""".stripMargin) { result =>
      val Memset = Sig.Extern("memset")
      val StringMemset = Global.Top("string").member(Memset)
      val decls = result.defns
        .collectFirst {
          case Defn.Declare(attrs, StringMemset, tpe) =>
            assert(attrs.isExtern)
            tpe shouldEqual Type.Function(
              Seq(Type.Ptr, Type.Int, Type.Long),
              Type.Ptr
            )
        }
        .orElse {
          fail("Not found extern declaration")
        }
    }
  }

  it should "define extern fields with correct attributes" in {
    testLinked(s"""
         |import scala.scalanative.unsafe._
         |
         |@extern trait lib {
         |  var field: CInt = extern
         |}
         |@extern object lib extends lib
         |
         |object Test {
         |  def main(args: Array[String]): Unit = {
         |     val read = lib.field
         |     lib.field = 42
         |  }
         |}""".stripMargin) { result =>
      val Field = Sig.Extern("field")
      val LibField = Global.Top("lib").member(Field)
      val decls = result.defns
        .collect {
          case defn @ Defn.Declare(attrs, LibField, tpe) =>
            assert(attrs.isExtern)
        }
      if (decls.isEmpty) fail("Not found extern declaration")
    }
  }

}
