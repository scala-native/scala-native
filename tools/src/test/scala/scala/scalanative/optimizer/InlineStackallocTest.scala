package scala.scalanative.optimizer

import scala.scalanative.OptimizerSpec
import scala.scalanative.interflow.Interflow.LLVMIntrinsics._
import scala.scalanative.nir._

import org.junit._
import org.junit.Assert._

class InlineStackallocTest extends OptimizerSpec {
  @Test def alwaysInlineStackallocNoLoop(): Unit = {
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
      ),
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFull)
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          println(defn.show)
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

  @Test def alwaysInlineStackallocWithLoop(): Unit = {
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
          |  def loop(n: Int): Unit = {
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
      ),
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFull)
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>
          println(defn.show)
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
  // TODO: nested stackallocs
  // TODO: allocation of multiple structs in loop and assignment to list

}
