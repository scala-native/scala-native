package scala.scalanative
package optimizer

import org.junit.Assert._
import org.junit._

import scala.scalanative.OptimizerSpec
import scala.scalanative.build.Mode

/** Boxes cross control-flow merges by value (#1529). */
class MergeBoxesTest extends OptimizerSpec {

  private val sources = Map(
    "Test.scala" ->
      """|import scala.scalanative.unsafe._
         |import scala.scalanative.unsigned._
         |
         |object Test {
         |  @noinline def loopAny(n: Int): Int = {
         |    var acc: Any = 0
         |    var i = 0
         |    while (i < n) { acc = acc.asInstanceOf[Int] + i; i += 1 }
         |    acc.asInstanceOf[Int]
         |  }
         |  @noinline def branch(n: Int): Int = {
         |    val x: Any = if (n > 0) n else -n
         |    x.asInstanceOf[Int]
         |  }
         |  @noinline def unsigned(n: Int): Long = {
         |    var acc = 0.toUInt
         |    var i = 0
         |    while (i < n) { acc += i.toUInt; i += 1 }
         |    acc.toLong
         |  }
         |  @noinline def pointers(start: Ptr[Byte], end: Ptr[Byte]): Int = {
         |    var ptr = start
         |    var sum = 0
         |    while (ptr != end) { sum += !ptr; ptr += 1 }
         |    sum
         |  }
         |  @noinline def nullPointers(start: Ptr[Byte], n: Int): Boolean = {
         |    var ptr = start
         |    var i = 0
         |    while (i < n) { ptr += 1; i += 1 }
         |    ptr == null
         |  }
         |  @noinline def boxIsNull(n: Int): Boolean = {
         |    val x: Any = n
         |    x == null
         |  }
         |  // The box is needed after the merge, where it is allocated once
         |  @noinline def escaping(n: Int): Any = if (n > 0) n else -n
         |  @noinline def aliased(n: Int): Boolean = {
         |    val x: AnyRef = if (n > 0) Integer.valueOf(n) else Integer.valueOf(-n)
         |    val y = x
         |    x eq y
         |  }
         |  // Boxes of equal values may be the same cached object
         |  @noinline def sameValue(n: Int): Boolean = {
         |    val x: Any = n
         |    val y: Any = n
         |    x.asInstanceOf[AnyRef] eq y.asInstanceOf[AnyRef]
         |  }
         |  @noinline def differentValues(): Boolean = {
         |    val x: Any = 1
         |    val y: Any = 2
         |    x.asInstanceOf[AnyRef] eq y.asInstanceOf[AnyRef]
         |  }
         |  // Unlike boxes of floating point numbers, which are never cached
         |  @noinline def sameDouble(n: Int): Boolean = {
         |    val x: Any = n.toDouble
         |    val y: Any = n.toDouble
         |    x.asInstanceOf[AnyRef] eq y.asInstanceOf[AnyRef]
         |  }
         |  // Instances of box classes created with `new` have identity
         |  @noinline def selectedInstance(flag: Boolean): Boolean = {
         |    val a = new java.lang.Double(1d)
         |    val b = new java.lang.Double(2d)
         |    val c = if (flag) a else b
         |    c eq a
         |  }
         |  // An allocated box entering a loop remains the same object
         |  @noinline def loopFromAllocated(init: java.lang.Integer, n: Int): Any = {
         |    var acc: Any = init
         |    var i = 0
         |    while (i < n) { acc = acc.asInstanceOf[Int] + i; i += 1 }
         |    acc
         |  }
         |  // `new Ptr` is never null, a boxed null pointer is
         |  @noinline def mixedPointers(flag: Boolean, base: Ptr[Byte], raw: Ptr[Byte]): Boolean = {
         |    val p = if (flag) base + 1 else !raw.asInstanceOf[Ptr[Ptr[Byte]]]
         |    p == null
         |  }
         |  def main(args: Array[String]): Unit = {
         |    val n = args.length
         |    println(loopAny(n) + branch(n) + unsigned(n))
         |    val buf = stackalloc[Byte](16)
         |    println(pointers(buf, buf + 16))
         |    println(nullPointers(null, n))
         |    println(boxIsNull(n))
         |    println(escaping(n))
         |    println(aliased(n))
         |    println(sameValue(n))
         |    println(differentValues())
         |    println(sameDouble(n))
         |    println(selectedInstance(n > 0))
         |    println(loopFromAllocated(Integer.valueOf(n), n))
         |    println(mixedPointers(n > 0, buf, buf))
         |  }
         |}
         |""".stripMargin
  )

  private def boxes(defn: nir.Defn.Define): Seq[nir.Op] =
    defn.insts.collect {
      case nir.Inst.Let(_, op @ (_: nir.Op.Box | _: nir.Op.Classalloc), _) => op
    }

  private def withMethod(name: String, modes: Mode*)(
      f: (Mode, nir.Defn.Define) => Unit
  ): Unit =
    modes.foreach { mode =>
      optimize("Test", sources, _.withMode(mode)) {
        case (_, result) =>
          f(mode, optimizedMethod(result.defns, "Test$", name))
      }
    }

  private def check(name: String, expectedBoxes: Int): Unit =
    withMethod(name, Mode.releaseFast, Mode.releaseFull) { (mode, defn) =>
      assertEquals(s"$mode\n${defn.show}", expectedBoxes, boxes(defn).size)
    }

  @Test def boxCarriedAroundLoop(): Unit = check("loopAny", 0)
  @Test def boxesOfBranches(): Unit = check("branch", 0)
  @Test def unsignedLocalInLoop(): Unit = check("unsigned", 0)
  @Test def pointerLocalInLoop(): Unit = check("pointers", 0)
  @Test def nullPointerEnteringLoop(): Unit = check("nullPointers", 0)
  @Test def boxEscapingAfterMerge(): Unit = check("escaping", 1)

  @Test def boxComparedWithNull(): Unit =
    withMethod("boxIsNull", Mode.releaseFull) { (_, defn) =>
      assertEquals(defn.show, Seq(nir.Val.False), returned(defn))
      assertEquals(defn.show, 0, boxes(defn).size)
    }

  @Test def aliasesOfMergedBox(): Unit =
    withMethod("aliased", Mode.releaseFull) { (_, defn) =>
      assertEquals(defn.show, Seq(nir.Val.True), returned(defn))
    }

  @Test def identityOfBoxesOfSameValue(): Unit =
    withMethod("sameValue", Mode.releaseFull) { (_, defn) =>
      assertTrue(defn.show, returned(defn).forall(!_.isCanonical))
    }

  @Test def identityOfBoxesOfDifferentValues(): Unit =
    withMethod("differentValues", Mode.releaseFull) { (_, defn) =>
      assertEquals(defn.show, Seq(nir.Val.False), returned(defn))
    }

  @Test def identityOfUncachedBoxes(): Unit =
    withMethod("sameDouble", Mode.releaseFull) { (_, defn) =>
      assertEquals(defn.show, Seq(nir.Val.False), returned(defn))
    }

  @Test def identityOfInstanceSelectedByBranch(): Unit =
    withMethod("selectedInstance", Mode.releaseFull) { (_, defn) =>
      assertTrue(defn.show, returned(defn).forall(!_.isCanonical))
    }

  @Test def allocatedBoxEnteringLoop(): Unit =
    withMethod("loopFromAllocated", Mode.releaseFull) { (_, defn) =>
      assertTrue(
        defn.show,
        returned(defn).forall(_.ty.isInstanceOf[nir.Type.Ref])
      )
    }

  @Test def pointerBoxesOfDifferentConstruction(): Unit =
    withMethod("mixedPointers", Mode.releaseFull) { (_, defn) =>
      assertEquals(defn.show, 2, boxes(defn).size)
      assertTrue(defn.show, returned(defn).forall(!_.isCanonical))
    }
}
