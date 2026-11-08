package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

/** Calling a member annotated with `@name("old_name")` emits the same reference
 *  as code compiled when `old_name` was the name of the extern itself.
 */
class ExternRedirectTest extends LinkerSpec {

  private val entry = "scala.scalanative.redirect.Test"

  private def externId(sig: nir.Sig): Option[String] = sig.unmangled match {
    case nir.Sig.Extern(id) => Some(id)
    case _                  => None
  }

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
                ) =>
              externId(sig)
          }.flatten
      }
      .flatten
      .toSet

  private def externDefns(
      result: ReachabilityAnalysis.Result,
      id: String
  ): Seq[nir.Defn] =
    result.defns.filter {
      case nir.Defn.Define(_, nir.Global.Member(_, sig), _, _, _) =>
        externId(sig).contains(id)
      case nir.Defn.Declare(_, nir.Global.Member(_, sig), _) =>
        externId(sig).contains(id)
      case _ => false
    }

  private def assertRedirect(
      result: ReachabilityAnalysis.Result,
      id: String
  ): Unit = {
    val defns = externDefns(result, id)
    assertTrue(s"$id is not defined", defns.nonEmpty)
    defns.foreach(defn => assertTrue(defn.show, defn.isExternRedirect))
  }

  private def linkRedirects(members: String, main: String)(
      fn: ReachabilityAnalysis.Result => Unit
  ): Unit =
    link(
      entry,
      s"""package scala.scalanative.redirect
         |import scala.scalanative.unsafe._
         |
         |@extern object Native {
         |  @name("new_name") def current(a: CInt, b: CLongLong): CInt = extern
         |$members
         |}
         |object Test {
         |  def main(args: Array[String]): Unit = $main
         |}
         |""".stripMargin
    )((_, result) => fn(result))

  @Test def forwardsCall(): Unit =
    linkRedirects(
      """  @name("old_name") def old(a: CInt, b: CLongLong): CInt =
        |    externRedirect(current(a, b))""".stripMargin,
      "println(Native.old(1, 2L))"
    ) { result =>
      assertRedirect(result, "old_name")
      assertTrue(calledExterns(result).contains("new_name"))
    }

  @Test def acceptsAnyBody(): Unit =
    linkRedirects(
      """  @name("old_name") def old(b: CLongLong, a: CInt, c: CInt): CLongLong =
        |    externRedirect {
        |      val shift = c * 2
        |      current(a + shift, 42L).toLong + b
        |    }""".stripMargin,
      "println(Native.old(1L, 2, 3))"
    ) { result =>
      assertRedirect(result, "old_name")
      assertTrue(calledExterns(result).contains("new_name"))
    }

  @Test def returnsUnit(): Unit =
    linkRedirects(
      """  @name("old_name") def old(a: CInt): Unit =
        |    externRedirect { current(a, 0L) }""".stripMargin,
      "Native.old(1)"
    )(assertRedirect(_, "old_name"))

  @Test def usesAttributesOfTarget(): Unit =
    link(
      entry,
      """package scala.scalanative.redirect
        |import scala.scalanative.unsafe._
        |
        |@extern object Native {
        |  @name("new_name") @blocking def current(v: CInt): CInt = extern
        |  @name("old_name") def old(v: CInt): CInt = externRedirect(current(v))
        |}
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.old(1))
        |}
        |""".stripMargin
    ) { (_, result) =>
      val target = result.infos.collectFirst {
        case (nir.Global.Member(_, sig), info)
            if externId(sig).contains("new_name") =>
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
        |  @name("old_name") def old(v: CInt): CInt = externRedirect(current(v))
        |}
        |@extern object Native extends NativeApi
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.old(1))
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
        |  @name("old_name") def old(v: CInt): CInt = externRedirect(current(v))
        |}
        |@extern object Native extends NativeApi
        |object Test {
        |  def main(args: Array[String]): Unit = println(Native.other())
        |}
        |""".stripMargin
    ) { (_, result) =>
      assertTrue(externDefns(result, "old_name").isEmpty)
      assertFalse(calledExterns(result).contains("new_name"))
    }
}
