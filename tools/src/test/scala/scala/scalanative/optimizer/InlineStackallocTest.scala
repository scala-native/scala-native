package scala.scalanative.optimizer

import scala.scalanative.OptimizerSpec
import scala.scalanative.interflow.Interflow.LLVMIntrinsics._
import scala.scalanative.nir._

import org.junit._
import org.junit.Assert._

class InlineStackallocTest extends OptimizerSpec {

  @Test def noLoop(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.annotation.alwaysinline
          |import scala.scalanative.unsafe._
          |
          |object Test {
          |  type Foo = CStruct2[Int, Int]
          |  @alwaysinline def init(): Ptr[Foo] = {
          |    val ptr = stackalloc[Foo]()
          |    ptr._1 = 21
          |    ptr._2 = 42
          |    ptr
          |  }
          |
          |  def doSomething(x: Ptr[Foo]): Unit = {
          |    val ptr = init()
          |    println(stackalloc[Int](64))
          |    val ptr2 = init()
          |    println((ptr, ptr2))
          |  }
          |
          |  def main(args: Array[String]): Unit = {
          |    val ptr = init()
          |    println(stackalloc[Int](64))
          |    val ptr2 = init()
          |    doSomething(ptr2)
          |    val ptr3 = init()
          |    assert(ptr == ptr3)
          |  }
          |}
          |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val stackallocId = defn.insts.collectFirst {
            case Inst.Let(id, Op.Stackalloc(_, _), _) => id
          }
          assertTrue("No stackalloc op", stackallocId.isDefined)

          val saveIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackSave, _), _) => id
          }
          assertTrue("No StackSave ops", saveIds.isEmpty)
          val restoreIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackRestore, _), _) => id
          }
          assertTrue("No StackRestore ops", restoreIds.isEmpty)
        }
    }
  }

  @Test def tailRecursiveLoop(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.annotation.alwaysinline
          |import scala.scalanative.unsafe._
          |
          |object Test {
          |  type Foo = CStruct2[Int, Int]
          |  @alwaysinline def init(): Ptr[Foo] = {
          |    val ptr = stackalloc[Foo]()
          |    ptr._1 = 21
          |    ptr._2 = 42
          |    ptr
          |  }
          |
          |
          |  @alwaysinline def loop(n: Int): Unit = {
          |    val ptr = init()
          |    println(stackalloc[Int](64))
          |    println(ptr)
          |    if (n > 0) loop(n - 1 )
          |  }
          |
          |  def main(args: Array[String]): Unit = {
          |    loop(10)
          |  }
          |}
          |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val stackallocId = defn.insts.collectFirst {
            case Inst.Let(id, Op.Stackalloc(_, _), _) => id
          }
          assertTrue("No stackalloc op", stackallocId.isDefined)

          val saveIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackSave, _), _) => id
          }
          assertTrue("No StackSave ops", saveIds.nonEmpty)
          assertEquals("StackSave ammount", 1, saveIds.size)
          val restoreIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackRestore, _), _) => id
          }
          assertTrue("No StackRestore ops", restoreIds.nonEmpty)
          assertEquals("StackRestore ammount", 1, restoreIds.size)
        }
    }
  }

  @Test def whileLoop(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.unsafe._
          |import scala.scalanative.unsigned._
          |import scala.scalanative.annotation.alwaysinline
          |
          |object Test {
          |  @alwaysinline def allocatingFunction(): Int = {
          |    val `64KB` = 64 * 1024
          |    val chunk = stackalloc[Byte](`64KB`)
          |    assert(chunk != null, "stackalloc was null")
          |    `64KB`
          |  }
          |
          |  def main(args: Array[String]): Unit = {
          |    val toAllocate = 32 * 1024 * 1024
          |    var allocated = 0
          |    while (allocated < toAllocate) {
          |      println(allocated)
          |      allocated += allocatingFunction()
          |    }
          |  }
          |}
          |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val stackallocId = defn.insts.collectFirst {
            case Inst.Let(id, Op.Stackalloc(_, _), _) => id
          }
          assertTrue("No stackalloc op", stackallocId.isDefined)

          val saveIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackSave, _), _) => id
          }
          assertTrue("No StackSave ops", saveIds.nonEmpty)
          assertEquals("StackSave ammount", 1, saveIds.size)

          val restoreIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackRestore, _), _) => id
          }
          assertTrue("No StackRestore ops", restoreIds.nonEmpty)
          assertEquals("StackRestore ammount", 1, restoreIds.size)
        }
    }
  }

  @Test def whileLoopNested(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.unsafe._
          |import scala.scalanative.unsigned._
          |import scala.scalanative.annotation.alwaysinline
          |
          |object Test {
          |  def main(args: Array[String]): Unit = {
          |    var i,j,k = 0
          |    while (i < 3) {
          |      val iAlloc = stackalloc[Byte](i)
          |      while(j < 3){
          |        val jAlloc = stackalloc[Short](j)
          |        while(k < 3){
          |          val kAlloc = stackalloc[Int](k)
          |          println((iAlloc, jAlloc, kAlloc))
          |          k += 1
          |        }
          |        j += 1
          |      }
          |      i += 1
          |    }
          |  }
          |}
          |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val stackallocId = defn.insts.collectFirst {
            case Inst.Let(id, Op.Stackalloc(_, _), _) => id
          }
          assertTrue("No stackalloc op", stackallocId.isDefined)

          val saveIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackSave, _), _) => id
          }
          assertTrue("No StackSave ops", saveIds.nonEmpty)
          assertEquals("StackSave ammount", 3, saveIds.size)

          val restoreIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackRestore, _), _) => id
          }
          assertTrue("No StackRestore ops", restoreIds.nonEmpty)
          assertEquals("StackRestore ammount", 3, restoreIds.size)
        }
    }
  }

  @Test def whileLoopMultipleNested(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.unsafe._
          |import scala.scalanative.unsigned._
          |
          |object Test {
          |  def main(args: Array[String]): Unit = {
          |    var i,j,k = 0
          |    while (i < 3) {
          |      val iAlloc = stackalloc[Ptr[Byte]](i)
          |      !iAlloc = stackalloc[Byte](i)
          |      while(j < 3){
          |        val jAlloc = stackalloc[Short](j)
          |        while(k < 3){
          |          val kAlloc = stackalloc[Ptr[Ptr[Int]]](k)
          |          !kAlloc = stackalloc[Ptr[Int]](k)
          |          !(!kAlloc) = stackalloc[Int](k)
          |          println((iAlloc, jAlloc, kAlloc))
          |          k += 1
          |        }
          |        j += 1
          |      }
          |      i += 1
          |    }
          |  }
          |}
          |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          val stackallocId = defn.insts.collectFirst {
            case Inst.Let(id, Op.Stackalloc(_, _), _) => id
          }
          assertTrue("No stackalloc op", stackallocId.isDefined)

          val saveIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackSave, _), _) => id
          }
          assertTrue("No StackSave ops", saveIds.nonEmpty)
          assertEquals("StackSave ammount", 3, saveIds.size)

          val restoreIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackRestore, _), _) => id
          }
          assertTrue("No StackRestore ops", restoreIds.nonEmpty)
          assertEquals("StackRestore ammount", 3, restoreIds.size)
        }
    }
  }
}
