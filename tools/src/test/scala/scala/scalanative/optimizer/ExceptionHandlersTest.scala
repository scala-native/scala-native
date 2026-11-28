package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec

class ExceptionHandlersTest extends OptimizerSpec {

  private def optimized(
      source: String,
      mode: build.Mode = build.Mode.releaseFast
  )(
      fn: Seq[nir.Defn] => Unit
  ): Unit =
    optimize(
      entry = "Test",
      sources = Map("Test.scala" -> source),
      setupConfig = _.withMode(mode)
    ) { case (_, result) => fn(result.defns) }

  private def calls(defn: nir.Defn.Define, name: String): Boolean =
    defn.insts.exists {
      case nir.Inst.Let(_, nir.Op.Call(_, nir.Val.Global(callee, _), _), _) =>
        callee.show.contains(s"D${name.length}$name")
      case _ => false
    }

  private def unwinds(defn: nir.Defn.Define): Seq[nir.Inst.Let] =
    defn.insts.collect {
      case inst @ nir.Inst.Let(_, _, _: nir.Next.Unwind) => inst
    }

  private def allocates(defn: nir.Defn.Define, cls: String): Boolean =
    defn.insts.exists {
      case nir.Inst.Let(_, nir.Op.Classalloc(name, _), _) => name.id == cls
      case _                                              => false
    }

  private def returnedInts(defn: nir.Defn.Define): Set[Int] =
    defn.insts.flatMap {
      case nir.Inst.Ret(nir.Val.Int(value)) => Seq(value)
      case nir.Inst.Jump(nir.Next.Label(_, Seq(nir.Val.Int(value)))) =>
        Seq(value)
      case _ => Nil
    }.toSet

  @Test def methodWithHandlerIsOptimized(): Unit = optimized(
    """|object Test {
       |  @noinline def opaque(x: Int): Int = x
       |  def twice(x: Int): Int = x + x
       |  @noinline def run(x: Int): Int =
       |    try twice(opaque(x))
       |    catch { case _: IllegalStateException => -1 }
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    assertEquals(run.show, nir.Attr.DidOpt, run.attrs.opt)
    assertFalse("twice is inlined\n" + run.show, calls(run, "twice"))
    assertTrue("opaque may throw\n" + run.show, calls(run, "opaque"))
    assertEquals(
      "only the call unwinds to the handler\n" + run.show,
      1,
      unwinds(run).size
    )
  }

  @Test def handlerIsDroppedWhenNothingThrows(): Unit = optimized(
    """|object Test {
       |  @noinline def run(x: Int): Int =
       |    try x + 1
       |    catch { case _: Throwable => -1 }
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    assertEquals(run.show, nir.Attr.DidOpt, run.attrs.opt)
    assertTrue(run.show, unwinds(run).isEmpty)
    assertFalse(run.show, returnedInts(run).contains(-1))
  }

  @Test def calleeWithHandlerIsInlined(): Unit = optimized(
    """|object Test {
       |  @noinline def opaque(x: Int): Int = x
       |  @inline def guarded(x: Int): Int =
       |    try opaque(x)
       |    catch { case _: IllegalStateException => -1 }
       |  @noinline def run(x: Int): Int = guarded(x) + guarded(x + 1)
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    assertFalse("guarded is inlined\n" + run.show, calls(run, "guarded"))
    assertEquals(run.show, 2, unwinds(run).size)
  }

  /* The exception thrown by an inlined callee is passed to the handler of
   * the call with a jump. What is still thrown is an exception of another
   * class, which the handler throws again.
   */
  @Test def thrownExceptionIsPassedToItsHandler(): Unit = optimized(
    """|object Test {
       |  final class Failure(val code: Int)
       |      extends Throwable(null, null, false, false)
       |  def check(x: Int): Int = {
       |    if (x < 0) throw new Failure(x)
       |    x
       |  }
       |  @noinline def run(x: Int): Int =
       |    try check(x)
       |    catch { case failure: Failure => failure.code + 1 }
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    assertFalse("check is inlined\n" + run.show, calls(run, "check"))
    assertFalse(
      "the exception reaches the handler without being thrown\n" + run.show,
      run.insts.exists {
        case nir.Inst.Throw(value, _) =>
          value.ty != nir.Rt.Throwable
        case _ => false
      }
    )
  }

  /* The handler sees the object as it was when the callee threw: after the
   * first update of the field, before the second one.
   */
  @Test def handlerSeesTheStateAtTheThrowingInstruction(): Unit = optimized(
    """|object Test {
       |  final class Box(var value: Int)
       |  @noinline def mayThrow(x: Int): Unit =
       |    if (x < 0) throw new IllegalStateException
       |  def update(box: Box, x: Int): Unit = {
       |    box.value += 5
       |    mayThrow(x)
       |    box.value += 7
       |  }
       |  @noinline def run(x: Int): Int = {
       |    val box = new Box(1)
       |    try { update(box, x); box.value += 1000 }
       |    catch { case _: IllegalStateException => box.value += 100 }
       |    box.value
       |  }
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    assertFalse(
      "the box is not allocated\n" + run.show,
      allocates(run, "Test$Box")
    )
    assertEquals(run.show, Set(1013, 106), returnedInts(run))
  }

  /* An object that only the handler lets escape is allocated on the
   * exceptional path. The handler does not return, otherwise the object would
   * be allocated on both paths when they merge.
   */
  @Test def allocationNeededByHandlerIsDelayedToIt(): Unit = optimized(
    """|object Test {
       |  final class Box(var value: Int)
       |  @noinline def mayThrow(x: Int): Int = {
       |    if (x < 0) throw new IllegalStateException
       |    x
       |  }
       |  @noinline def escape(box: Box): Int = box.value
       |  @noinline def run(x: Int): Int = {
       |    val box = new Box(x)
       |    try mayThrow(x)
       |    catch {
       |      case _: IllegalStateException =>
       |        escape(box)
       |        throw new Error
       |    }
       |  }
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    val cfg = nir.ControlFlow.Graph(run.insts)
    val allocating = cfg.all.filter(_.insts.exists {
      case nir.Inst.Let(_, nir.Op.Classalloc(name, _), _) =>
        name.id == "Test$Box"
      case _ => false
    })
    assertEquals(run.show, 1, allocating.size)
    assertFalse(
      "the entry block does not allocate\n" + run.show,
      allocating.contains(cfg.entry)
    )
    assertFalse(
      "the block of the call does not allocate\n" + run.show,
      allocating.head.insts.exists {
        case nir.Inst.Let(_, _: nir.Op.Call, _: nir.Next.Unwind) => true
        case _                                                   => false
      }
    )
  }

  /* A stack allocation outside of the entry block makes a frame dynamically
   * sized, and such a frame cannot be relocated when a virtual thread is
   * suspended in it. Methods with handlers were not inlined into, so they keep
   * the frames they had.
   */
  @Test def stackAllocationIsNotInlinedIntoMethodWithHandlers(): Unit =
    optimized(
      """|import scala.scalanative.unsafe._
         |object Test {
         |  @noinline def opaque(x: Int): Int = x
         |  @inline def onStack(x: Int): Int = {
         |    val ptr = stackalloc[Int]()
         |    !ptr = opaque(x)
         |    !ptr
         |  }
         |  @noinline def guarded(x: Int): Int =
         |    try onStack(x)
         |    catch { case _: IllegalStateException => -1 }
         |  @noinline def plain(x: Int): Int = onStack(x)
         |  def main(args: Array[String]): Unit =
         |    println(guarded(args.length) + plain(args.length))
         |}
         |""".stripMargin
    ) { defns =>
      val guarded = optimizedMethod(defns, "Test$", "guarded")
      val plain = optimizedMethod(defns, "Test$", "plain")
      assertTrue(guarded.show, calls(guarded, "onStack"))
      assertFalse(guarded.show, guarded.hasStackalloc)
      assertFalse(plain.show, calls(plain, "onStack"))
      assertTrue(plain.show, plain.hasStackalloc)
    }

  @Test def stackAllocationIsNotInlinedAfterCalleeWithHandlers(): Unit =
    optimized(
      """|import scala.scalanative.unsafe._
         |object Test {
         |  @noinline def opaque(x: Int): Int = x
         |  @inline def onStack(x: Int): Int = {
         |    val ptr = stackalloc[Int]()
         |    !ptr = opaque(x)
         |    !ptr
         |  }
         |  @inline def guarded(x: Int): Int =
         |    try opaque(x)
         |    catch { case _: IllegalStateException => -1 }
         |  @noinline def run(x: Int): Int = guarded(x) + onStack(x)
         |  def main(args: Array[String]): Unit = println(run(args.length))
         |}
         |""".stripMargin
    ) { defns =>
      val run = optimizedMethod(defns, "Test$", "run")
      assertFalse(run.show, calls(run, "guarded"))
      assertTrue(run.show, calls(run, "onStack"))
      assertFalse(run.show, run.hasStackalloc)
    }

  @Test def debugModeLeavesHandlersAlone(): Unit = optimized(
    """|object Test {
       |  def twice(x: Int): Int = x + x
       |  @noinline def run(x: Int): Int =
       |    try twice(x)
       |    catch { case _: IllegalStateException => -1 }
       |  def main(args: Array[String]): Unit = println(run(args.length))
       |}
       |""".stripMargin,
    mode = build.Mode.debug
  ) { defns =>
    val run = optimizedMethod(defns, "Test$", "run")
    assertNotEquals(run.show, nir.Attr.DidOpt, run.attrs.opt)
  }
}
