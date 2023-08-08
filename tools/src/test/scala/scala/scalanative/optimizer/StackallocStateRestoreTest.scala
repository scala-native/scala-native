package scala.scalanative.optimizer

import scala.scalanative.OptimizerSpec
import scala.scalanative.interflow.Interflow.LLVMIntrinsics._
import scala.scalanative.nir._

import org.junit._
import org.junit.Assert._

class StackallocStateRestoreTest extends OptimizerSpec {

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
      ),
      // Test is releaseMode to make it inline more
      setupConfig = _.withMode(scala.scalanative.build.Mode.releaseFast)
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

  @Test def escapingStackalloc(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.unsafe._
          |object Test {
          |  def main(args: Array[String]): Unit = {
          |    println("Hello, World!")
          |
          |    import CList._
          |    var i = 0
          |    var head: Ptr[Node] = null
          |    while (i < 4) {
          |      head = stackalloc[Node]().init(i, head)
          |      println(head)
          |      i += 1
          |    }
          |    println(head)
          |  }
          |}
          |
          |object CList {
          |  type Node = CStruct2[Int, Ptr[_]]
          |
          |  implicit class NodeOps(val self: Ptr[Node]) extends AnyVal {
          |    def init(value: Int, next: Ptr[Node]) = {
          |      self._1 = value
          |      self._2 = next
          |      self
          |    }
          |  }
          |}
          |
          |""".stripMargin
      )
    ) {
      case (_, result) =>
        findEntry(result.defns).foreach { defn =>

          val stackallocId = defn.insts.collectFirst {
            case Inst.Let(id, Op.Stackalloc(_, _), _) => id
          }
          assertTrue("No stackalloc op", stackallocId.nonEmpty)

          val saveIds = defn.insts.collect {
            case Inst.Let(id, Op.Call(_, StackSave, _), _) => id
          }
          assertTrue("No StackSave ops", saveIds.isEmpty)
        }
    }
  }

  @Test def escapingStackalloc2(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.unsafe._
          |object Test {
          |  def main(args: Array[String]): Unit = {
          |    println("Hello, World!")
          |
          |    import CList._
          |    var i, j = 0
          |    i = args.headOption.map(_.toInt).getOrElse(0)
          |    while (i < 4) {
          |      j = 0
          |      var head: Ptr[Node] = null
          |      head = stackalloc[Node]().init(-1, head)
          |      while (j < 4) {
          |        head = stackalloc[Node]().init(j, head)
          |        println(head)
          |        j += 1
          |      }
          |      i += 1
          |    }
          |  }
          |}
          |
          |object CList {
          |  type Node = CStruct2[Int, Ptr[_]]
          |
          |  implicit class NodeOps(val self: Ptr[Node]) extends AnyVal {
          |    def init(value: Int, next: Ptr[Node]) = {
          |      self._1 = value
          |      self._2 = next
          |      self
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

  @Test def escapingStackalloc3(): Unit = {
    optimize(
      entry = "Test",
      sources = Map(
        "Test.scala" -> """
          |import scala.scalanative.unsafe._
          |object Test {
          |  def main(args: Array[String]): Unit = {
          |    println("Hello, World!")
          |
          |    import CList._
          |    var i, j = 0
          |    i = args.headOption.map(_.toInt).getOrElse(0)
          |    while (i < 4) {
          |      j = 0
          |      var head: Ptr[Node] = null
          |      // No outer stackalloc // head = stackalloc[Node]().init(-1, head)
          |      while (j < 4) {
          |        head = stackalloc[Node]().init(j, head)
          |        println(head)
          |        j += 1
          |      }
          |      i += 1
          |    }
          |  }
          |}
          |
          |object CList {
          |  type Node = CStruct2[Int, Ptr[_]]
          |
          |  implicit class NodeOps(val self: Ptr[Node]) extends AnyVal {
          |    def init(value: Int, next: Ptr[Node]) = {
          |      self._1 = value
          |      self._2 = next
          |      self
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

}
