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

  @Test def rewritesReferencesToTarget(): Unit =
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
      assertNotExtern(result, "old_name")
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
      assertNotExtern(result, "old_name")
      assertTrue(calledExterns(result).contains("new_name"))
    }

  @Test def unusedAliasIsNotExported(): Unit =
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
      """package scala.scalanative.redirect
        |import scala.scalanative.libc.locale
        |import scala.scalanative.posix.{signal, time}
        |import scala.scalanative.posix.sys.socket
        |import scala.scalanative.unsafe._
        |
        |object Test {
        |  def main(args: Array[String]): Unit = {
        |    println(time.clock_gettime_compat(0, null))
        |    println(time.nanosleep_compat(null, null))
        |    println(socket.socket_compat(0, 0, 0))
        |    println(signal.sigaltstack_compat(null, null))
        |    println(locale.setlocale_compat(0, null))
        |  }
        |}
        |""".stripMargin
    ) { (_, result) =>
      Seq("clock_gettime", "nanosleep", "socket", "sigaltstack", "setlocale")
        .foreach { id =>
          assertNotExtern(result, id)
          assertTrue(
            s"scalanative_$id not called",
            calledExterns(result).contains(s"scalanative_$id")
          )
        }
    }
}
