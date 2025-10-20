package scala.scalanative.nir

import org.junit.Test
import org.junit.Assert.*

import scala.scalanative.LinkerSpec

class PrivateMethodsManglingSuite extends LinkerSpec {

  @Test def nestedManglingOfPrivateMethods(): Unit = {
    val sources = Map(
      "A.scala" -> """
		|package xyz
		|abstract class A {
		|  protected def encodeLoop(arg1: Int, arg2: String): String
		|  final def encode(arg1: Int, arg2: String, arg3: Boolean): String = {
		|    def loop(): String = {
		|      if (arg3) "hello"
		|      else encodeLoop(arg1, arg2)
		|    }
		|    loop()
		|  }
		|  private def foo(x: Int): Int = x + 1
		|  def bar(x: Int): Int         = foo(x)
		|}
		|""".stripMargin,
      "B.scala" -> """
  		|package xyz
  		|object B extends A {
  		|  def encodeLoop(arg1: Int, arg2: String): String = {
  		|    println("init_logic")
  		|    val bool: Boolean = false
  		|    def loop(): String = {
  		|      if (bool) "asd" else arg2 * arg1
  		|    }
  		|    loop()
  		|  }
  		|  private def foo(x: Int): Int = x * 2
  		|  def baz(x: Int): Int         = foo(x)
  		|}
		|""".stripMargin,
      "C.scala" -> """
  		|package foo
  		|object B extends xyz.A {
  		|  def encodeLoop(arg1: Int, arg2: String): String = {
  		|    println("init_logic")
  		|    val bool: Boolean = false
  		|    def loop(): String = {
  		|      if (bool) "asd" else arg2 * arg1
  		|    }
  		|    loop()
  		|  }
  		|  private def foo(x: Int): Int = x * 2
  		|  def fooBar(x: Int): Int      = foo(x)
  		|}
		|""".stripMargin,
      "Main.scala" -> """
  		|object Main {
  		|  def main(args: Array[String]): Unit = {
  		|    xyz.B.encode(1, "asd", true)
  		|    xyz.B.baz(1)
  		|    foo.B.encode(1, "asd", true)
  		|    foo.B.fooBar(1)
  		|    xyz.B.bar(1)
  		|  }
  		|}
		|""".stripMargin
    )

    val tops = Seq("xyz.B$", "xyz.A", "foo.B$").map(Global.Top(_))

    link("Main", sources) {
      case (_, result) =>
        val testedDefns = result.defns
          .collect {
            case Defn.Define(_, Global.Member(owner, sig), _, _, _)
                if tops.contains(owner) =>
              sig.unmangled
          }
          .collect {
            case sig: Sig.Method => sig
          }
          .toSet

        val loopType = Seq(Type.Int, Rt.String, Type.Bool, Rt.String)
        val fooType = Seq(Type.Int, Type.Int)

        def privateMethodSig(method: String, tpe: Seq[Type], in: String) = {
          Sig.Method(method, tpe, Sig.Scope.Private(Global.Top(in)))
        }

        val expected = Seq(
          Sig.Method("encode", Seq(Type.Int, Rt.String, Type.Bool, Rt.String)),
          Sig.Method("encodeLoop", Seq(Type.Int, Rt.String, Rt.String)),
          privateMethodSig("loop$1", loopType, "xyz.B$"),
          privateMethodSig("loop$1", loopType, "xyz.A"),
          privateMethodSig("loop$1", loopType, "foo.B$"),
          privateMethodSig("foo", fooType, "xyz.A"),
          privateMethodSig("foo", fooType, "xyz.B$"),
          privateMethodSig("foo", fooType, "foo.B$")
        )

        expected.foreach {
          case sig @ Sig.Method(id, tpe, scope) =>
            def containsExactlySig = testedDefns.contains(sig)
            // In 2.12, the order of method arguments in closures has changed,
            // that's why this "hack" is needed.
            def containsSig = testedDefns.exists {
              case Sig.Method(`id`, sigTpe, `scope`)
                  if sigTpe.toSet == tpe.toSet =>
                true
              case _ => false
            }
            assertTrue(containsExactlySig || containsSig)
        }
    }

  }
}
