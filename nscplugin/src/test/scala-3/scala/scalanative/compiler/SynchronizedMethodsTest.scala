package scala.scalanative
package compiler

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.nir._

class SynchronizedMethodsTest {

  /** `SimplifySynchronized` landed in Scala 3.10 and sets `Synchronized` on
   *  methods whose body is a single `this.synchronized` block.
   */
  private def hasSimplifySynchronized: Boolean =
    scalaVersion.split('.')(1).takeWhile(_.isDigit).toInt >= 10

  private val RuntimePackageCls =
    Global.Top("scala.scalanative.runtime.package")
  private val EnterMonitorSig =
    Sig
      .Method(
        "enterMonitor",
        Seq(Rt.Object, Type.Unit),
        Sig.Scope.PublicStatic
      )
      .mangled

  private def enterMonitorArgs(defn: Defn.Define): Seq[Seq[Val]] =
    defn.insts.collect {
      case Inst.Let(
            _,
            Op.Call(
              _,
              Val.Global(Global.Member(RuntimePackageCls, sig), _),
              args
            ),
            _
          ) if sig == EnterMonitorSig =>
        args
    }

  @Test def staticSynchronizedMethodLocksOnClass(): Unit = {
    compileAndLoad(
      "A.scala" ->
        """|
           |class A
           |object A {
           |  @scala.annotation.static
           |  def yesStaticOne: Int = synchronized {
           |    1
           |  }
           |}
           |""".stripMargin
    ) { defns =>
      val cls = Global.Top("A")
      val method = cls.member(
        Sig.Method("yesStaticOne", Seq(Type.Int), Sig.Scope.PublicStatic)
      )
      val defn = defns.collectFirst {
        case d: Defn.Define if d.name == method => d
      }
      assertTrue(s"missing $method in ${defns.map(_.name)}", defn.isDefined)

      val locks = enterMonitorArgs(defn.get)
      assertEquals("expected one monitor enter", 1, locks.size)
      if (hasSimplifySynchronized) {
        assertEquals(
          "static synchronized methods lock on the enclosing Class",
          Seq(Val.ClassOf(cls)),
          locks.head
        )
      }
    }
  }

  @Test def instanceSynchronizedMethodLocksOnThis(): Unit = {
    compileAndLoad(
      "C.scala" ->
        """|
           |class C {
           |  def yesBasic: Int = synchronized {
           |    1
           |  }
           |}
           |""".stripMargin
    ) { defns =>
      val cls = Global.Top("C")
      val method = cls.member(Sig.Method("yesBasic", Seq(Type.Int)))
      val defn = defns.collectFirst {
        case d: Defn.Define if d.name == method => d
      }
      assertTrue(s"missing $method in ${defns.map(_.name)}", defn.isDefined)

      val locks = enterMonitorArgs(defn.get)
      assertEquals("expected one monitor enter", 1, locks.size)
      locks.head match {
        case Seq(Val.Local(_, Type.Ref(name, _, _))) =>
          assertEquals(cls, name)
        case other =>
          fail(s"expected lock on this: $other")
      }
    }
  }
}
