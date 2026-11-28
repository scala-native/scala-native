package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec
import scala.scalanative.build.Mode

/** Boxes are passed by value to the specialized, non-inlined callees (#1529).
 */
class UnboxedArgumentsTest extends OptimizerSpec {

  private val sources = Map(
    "Test.scala" ->
      """|object Test {
         |  var sink: Any = null
         |  // Uses only the value of the box
         |  @noinline def valueOnly(x: Any, n: Int): Int = x match {
         |    case i: Int => i + n
         |    case _      => -1
         |  }
         |  // Needs the object
         |  @noinline def escaping(x: Any): Unit = sink = x
         |  @noinline def callsValueOnly(n: Int): Int = valueOnly(n, n)
         |  // Universal equality of a box does not need the object
         |  @noinline def compared(x: Any, y: Any): Boolean = x == y
         |  @noinline def callsCompared(n: Int, y: Any): Boolean = compared(n, y)
         |  // Needs the identity of the object
         |  @noinline def same(x: Any, y: Any): Boolean =
         |    x.asInstanceOf[AnyRef] eq y.asInstanceOf[AnyRef]
         |  @noinline def callsSame(n: Int, y: Any): Boolean = same(n, y)
         |  @noinline def callsSameTwice(n: Int): Boolean = same(n, n)
         |  @noinline def callsEscaping(n: Int): Unit = escaping(n)
         |  @noinline def callsChain(n: Long): Int = chain(n, 3)
         |  @noinline def chain(x: Any, n: Int): Int =
         |    if (n == 0) x.hashCode() else chain(x, n - 1) + 1
         |  def main(args: Array[String]): Unit = {
         |    val n = args.length
         |    println(callsValueOnly(n))
         |    println(callsCompared(n, args))
         |    println(callsSame(n, args))
         |    println(callsSameTwice(n))
         |    println(same(args, n))
         |    println(compared(args, n))
         |    callsEscaping(n)
         |    println(callsChain(n))
         |    println(valueOnly("foo", n))
         |    escaping("foo")
         |    println(chain("foo", n))
         |    println(sink)
         |  }
         |}
         |""".stripMargin
  )

  private def boxes(defn: nir.Defn.Define): Seq[nir.Op.Box] =
    defn.insts.collect { case nir.Inst.Let(_, op: nir.Op.Box, _) => op }

  private def calls(defn: nir.Defn.Define): Seq[nir.Op.Call] =
    defn.insts.collect { case nir.Inst.Let(_, op: nir.Op.Call, _) => op }

  private def releaseFull(
      fn: Seq[nir.Defn] => Unit
  ): Unit =
    optimize("Test", sources, _.withMode(Mode.releaseFull)) {
      case (_, result) => fn(result.defns)
    }

  private def original(name: nir.Global): nir.Global = name match {
    case nir.Global.Member(owner, sig) =>
      sig.unmangled match {
        case nir.Sig.Duplicate(of, _) => nir.Global.Member(owner, of)
        case _                        => name
      }
    case _ => name
  }

  private def callee(defns: Seq[nir.Defn], call: nir.Op.Call) = {
    val nir.Val.Global(name, _) = call.ptr: @unchecked
    defns.collectFirst { case d: nir.Defn.Define if d.name == name => d }.get
  }

  @Test def boxPassedByValue(): Unit = releaseFull { defns =>
    val caller = optimizedMethod(defns, "Test$", "callsValueOnly")
    assertEquals(caller.show, Nil, boxes(caller))
    val Seq(call) = calls(caller): @unchecked
    val nir.Type.Function(Seq(_, argty, _), _) = call.ty: @unchecked
    assertEquals(caller.show, nir.Type.Int, argty)
    val target = callee(defns, call)
    assertEquals(target.show, call.ty, target.ty)
    assertEquals(target.show, Nil, boxes(target))
  }

  @Test def boxComparedByValue(): Unit = releaseFull { defns =>
    val caller = optimizedMethod(defns, "Test$", "callsCompared")
    assertEquals(caller.show, Nil, boxes(caller))
    val Seq(call) = calls(caller): @unchecked
    val target = callee(defns, call)
    assertEquals(target.show, nir.Type.Int, target.ty.args(1))
    assertEquals(target.show, Nil, boxes(target))
  }

  // The callee needing an object gets the box allocated by the caller,
  // otherwise each callee the box is passed through would allocate it again.
  private def checkAllocatedByCaller(name: String): Unit = releaseFull {
    defns =>
      val caller = optimizedMethod(defns, "Test$", name)
      val Seq(call) = calls(caller): @unchecked
      val target = callee(defns, call)
      assertEquals(caller.show, 1, boxes(caller).size)
      assertEquals(target.show, Nil, boxes(target))
      // The rejected duplicate taking the box by value is not emitted
      val unboxed = defns.collect {
        case defn: nir.Defn.Define
            if original(defn.name) == original(target.name) &&
              !defn.ty.args(1).isInstanceOf[nir.Type.RefKind] =>
          defn.name
      }
      assertEquals(Nil, unboxed)
  }

  @Test def boxComparedByIdentity(): Unit =
    checkAllocatedByCaller("callsSame")

  // Boxes of the same small value are the same cached instance
  @Test def boxesComparedByIdentity(): Unit = releaseFull { defns =>
    val caller = optimizedMethod(defns, "Test$", "callsSameTwice")
    val Seq(call) = calls(caller): @unchecked
    assertEquals(caller.show, 1, boxes(caller).size)
    assertTrue(caller.show, call.ty.args(1).isInstanceOf[nir.Type.RefKind])
    assertTrue(caller.show, call.ty.args(2).isInstanceOf[nir.Type.RefKind])
  }

  @Test def boxNeededByCallee(): Unit =
    checkAllocatedByCaller("callsEscaping")

  // The box needed only on some paths is allocated there by the callee,
  // and only once: the recursive calls take the allocated box.
  @Test def boxPassedThroughRecursiveCalls(): Unit = releaseFull { defns =>
    val caller = optimizedMethod(defns, "Test$", "callsChain")
    val Seq(call) = calls(caller): @unchecked
    val target = callee(defns, call)
    assertEquals(caller.show, Nil, boxes(caller))
    assertEquals(target.show, nir.Type.Long, target.ty.args(1))
    assertEquals(target.show, 1, boxes(target).size)
    val recursive = calls(target).filter { call =>
      original(call.ptr.asInstanceOf[nir.Val.Global].name) ==
        original(target.name)
    }
    assertEquals(target.show, 1, recursive.size)
    recursive.foreach { call =>
      assertTrue(target.show, call.ty.args(1).isInstanceOf[nir.Type.RefKind])
    }
  }

  @Test def unspecializedInOtherModes(): Unit =
    optimize("Test", sources, _.withMode(Mode.releaseFast)) {
      case (_, result) =>
        val caller = optimizedMethod(result.defns, "Test$", "callsValueOnly")
        assertEquals(caller.show, 1, boxes(caller).size)
    }
}
