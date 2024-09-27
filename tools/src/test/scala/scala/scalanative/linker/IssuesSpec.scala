package scala.scalanative
package linker

import scala.scalanative.checker.Check
import scala.scalanative.LinkerSpec

import org.junit.Test
import org.junit.Assert._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class IssuesSpec extends LinkerSpec {

  private val mainClass = "Test"
  private val sourceFile = "Test.scala"

  private def testLinked(source: String, mainClass: String = mainClass)(
      fn: ReachabilityAnalysis.Result => Unit
  ): Unit =
    link(mainClass, sources = Map("Test.scala" -> source)) {
      case (_, result) => fn(result)
    }

  private def checkNoLinkageErrors(
      source: String,
      mainClass: String = mainClass
  ) =
    testLinked(source.stripMargin, mainClass) { result =>
      val check = Check(result)
      val errors = Await.result(check, 1.minute)
      assertTrue(errors.isEmpty)
    }

  @Test def mainClassWithEncodedChars(): Unit = {
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

  @Test def issue2880LambadHandling(): Unit = checkNoLinkageErrors {
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

  @Test def externTraitsPrimitveTypesSignatures(): Unit = {
    testLinked(s"""
      |import scala.scalanative.unsafe._
      |import scala.scalanative.unsigned._
      |
      |@extern trait string {
      |  def memset(dest: Ptr[Byte], ch: Int, count: USize): Ptr[Byte] = extern
      |}
      |@extern object string extends string
      |
      |object Test {
      |  def main(args: Array[String]): Unit = {
      |     val privilegeSetLength = stackalloc[USize]()
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
      val Memset = nir.Sig.Extern("memset")
      val StringMemset = nir.Global.Top("string").member(Memset)
      val decls = result.defns
        .collectFirst {
          case nir.Defn.Declare(attrs, StringMemset, tpe) =>
            assertTrue(attrs.isExtern)
            assertEquals(
              nir.Type.Function(
                Seq(nir.Type.Ptr, nir.Type.Int, nir.Type.Size),
                nir.Type.Ptr
              ),
              tpe
            )
        }
        .orElse {
          fail("Not found extern declaration")
          ???
        }
    }
  }

  @Test def externTraitExternFieldAttributes(): Unit = {
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
      val Field = nir.Sig.Extern("field")
      val LibField = nir.Global.Top("lib").member(Field)
      val decls = result.defns
        .collect {
          case defn @ nir.Defn.Declare(attrs, LibField, tpe) =>
            assertTrue(attrs.isExtern)
        }
      if (decls.isEmpty) fail("Not found extern declaration")
    }
  }

  @Test def externTraitBlockingMethodAttributes(): Unit = {
    testLinked(s"""
      |import scala.scalanative.unsafe._
      |
      |@extern object lib {
      |  @blocking def sync(): CInt = extern
      |  def async(): CInt = extern
      |}
      |
      |@extern @blocking object syncLib{
      |  def foo(): CInt = extern
      |}
      |
      |object Test {
      |  def main(args: Array[String]): Unit = {
      |     val a = lib.sync()
      |     val b = lib.async()
      |     val c = syncLib.foo()
      |  }
      |}""".stripMargin) { result =>
      val Lib = nir.Global.Top("lib$")
      val SyncLib = nir.Global.Top("syncLib$")
      val LibSync = Lib.member(nir.Sig.Extern("sync"))
      val LibAsync = Lib.member(nir.Sig.Extern("async"))
      val SyncLibFoo = SyncLib.member(nir.Sig.Extern("foo"))

      val found = result.defns
        .collect {
          case nir.Defn.Declare(attrs, LibSync, _) =>
            assertTrue(attrs.isExtern && attrs.isBlocking)
          case nir.Defn.Declare(attrs, LibAsync, _) =>
            assertTrue(attrs.isExtern && !attrs.isBlocking)
          case nir.Defn.Declare(attrs, SyncLibFoo, _) =>
            assertTrue(attrs.isExtern && attrs.isBlocking)
        }
      assertEquals(3, found.size)
    }
  }

  // https://github.com/scala-native/scala-native/issues/4039
  @Test def issue4039(): Unit = checkNoLinkageErrors {
    """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    Seq.empty[Nothing].forall(_ == Int.box(0))
    |  }
    |}
    |"""
  }
}
