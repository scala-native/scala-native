package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

/** Calling a member annotated with `@name("old_name")` emits the same reference
 *  as code compiled when `old_name` was the name of the extern itself.
 */
class ExternRedirectTest extends LinkerSpec {

  private val entry = "scala.scalanative.redirect.Test"

  private def calledExterns(result: ReachabilityAnalysis.Result): Set[String] =
    result.defns
      .collect {
        case defn: nir.Defn.Define =>
          defn.insts.collect {
            case nir.Inst.Let(
                  _,
                  nir.Op.Call(
                    _,
                    nir.Val.Global(nir.Global.Member(_, sig), _),
                    _
                  ),
                  _
                ) if sig.isExtern =>
              sig.unmangled match {
                case nir.Sig.Extern(id) => id
                case _                  => fail("expected extern"); ""
              }
          }
      }
      .flatten
      .toSet

  private def definedExterns(result: ReachabilityAnalysis.Result): Set[String] =
    result.defns.collect {
      case nir.Defn.Define(_, nir.Global.Member(_, sig), _, _, _)
          if sig.isExtern =>
        sig.unmangled match {
          case nir.Sig.Extern(id) => id
          case _                  => fail("expected extern"); ""
        }
    }.toSet

  private def assertNotExtern(
      result: ReachabilityAnalysis.Result,
      id: String
  ): Unit = {
    assertFalse(s"$id called", calledExterns(result).contains(id))
    assertFalse(s"$id defined", definedExterns(result).contains(id))
    assertFalse(
      s"$id still in infos",
      result.infos.keys.exists {
        case nir.Global.Member(_, sig) => sig == nir.Sig.Extern(id).mangled
        case _                         => false
      }
    )
  }

  private def assertRedirect(
      result: ReachabilityAnalysis.Result,
      id: String
  ): Unit = {
    val defns = result.defns.filter {
      case nir.Defn.Define(_, nir.Global.Member(_, sig), _, _, _) =>
        sig == nir.Sig.Extern(id).mangled
      case nir.Defn.Declare(_, nir.Global.Member(_, sig), _) =>
        sig == nir.Sig.Extern(id).mangled
      case _ => false
    }
    assertTrue(s"$id is not a redirect", defns.nonEmpty)
    defns.foreach(defn => assertTrue(defn.show, defn.isExternRedirect))
    defns.foreach { defn =>
      assertEquals(nir.Attr.AlwaysInline, defn.attrs.inlineHint)
    }
  }

  @Test def generatesFunctionCallingTarget(): Unit =
    link(
      entry,
      """package scala.scalanative.redirect
        |import scala.scalanative.unsafe._
        |
        |@extern object Native {
        |  @name("new_name") def current(v: CInt): CInt = extern
        |  @name("old_name") private[scalanative] def compat(v: CInt): CInt =
        |    externRedirect(current(v))
        |}
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.compat(1))
        |}
        |""".stripMargin
    ) { (_, result) =>
      assertRedirect(result, "old_name")
      assertTrue(calledExterns(result).contains("new_name"))
    }

  @Test def usesAttributesOfTarget(): Unit =
    link(
      entry,
      """package scala.scalanative.redirect
        |import scala.scalanative.unsafe._
        |
        |@extern object Native {
        |  @name("new_name") @blocking def current(v: CInt): CInt = extern
        |  @name("old_name") private[scalanative] def compat(v: CInt): CInt =
        |    externRedirect(current(v))
        |}
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.compat(1))
        |}
        |""".stripMargin
    ) { (_, result) =>
      val target = result.infos.collectFirst {
        case (nir.Global.Member(_, sig), info)
            if sig == nir.Sig.Extern("new_name").mangled =>
          info
      }
      assertTrue(target.exists(_.attrs.isBlocking))
    }

  @Test def supportsExternTraits(): Unit =
    link(
      entry,
      """package scala.scalanative.redirect
        |import scala.scalanative.unsafe._
        |
        |@extern trait NativeApi {
        |  @name("new_name") def current(v: CInt): CInt = extern
        |  @name("old_name") private[scalanative] def compat(v: CInt): CInt =
        |    externRedirect(current(v))
        |}
        |@extern object Native extends NativeApi
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.compat(1))
        |}
        |""".stripMargin
    ) { (_, result) =>
      assertRedirect(result, "old_name")
      assertTrue(calledExterns(result).contains("new_name"))
    }

  @Test def unusedRedirectIsNotExported(): Unit =
    link(
      entry,
      """package scala.scalanative.redirect
        |import scala.scalanative.unsafe._
        |
        |@extern trait NativeApi {
        |  def other(): CInt = extern
        |  @name("new_name") def current(v: CInt): CInt = extern
        |  @name("old_name") private[scalanative] def compat(v: CInt): CInt =
        |    externRedirect(current(v))
        |}
        |@extern object Native extends NativeApi
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.other())
        |}
        |""".stripMargin
    ) { (_, result) =>
      assertNotExtern(result, "old_name")
      assertFalse(calledExterns(result).contains("new_name"))
    }

  @Test def redirectsRenamedLibraryExterns(): Unit =
    link(
      entry,
      Map(
        "posix.scala" ->
          """package scala.scalanative.posix
            |
            |object Compat {
            |  def run(): Unit = {
            |    println(time._clock_gettime(0, null))
            |    println(time._nanosleep(null, null))
            |    println(sys.socket._socket(0, 0, 0))
            |    println(signal._sigaltstack(null, null))
            |  }
            |}
            |""".stripMargin,
        "libc.scala" ->
          """package scala.scalanative.libc
            |
            |object Compat {
            |  def run(): Unit = println(locale._setlocale(0, null))
            |}
            |""".stripMargin,
        "test.scala" ->
          """package scala.scalanative.redirect
            |
            |object Test {
            |  def main(args: Array[String]): Unit = {
            |    scala.scalanative.posix.Compat.run()
            |    scala.scalanative.libc.Compat.run()
            |  }
            |}
            |""".stripMargin
      )
    ) { (_, result) =>
      Seq("clock_gettime", "nanosleep", "socket", "sigaltstack", "setlocale")
        .foreach { id =>
          assertRedirect(result, id)
          assertTrue(
            s"scalanative_$id not called",
            calledExterns(result).contains(s"scalanative_$id")
          )
        }
    }
}
