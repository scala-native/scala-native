package scala.scalanative.memory

import java.nio.file.Files

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import scala.scalanative.NIRCompiler
import scala.scalanative.api.CompilationFailedException

class SafeZoneTest extends AnyFlatSpec with Matchers {
  def nativeCompilation(source: String): Unit = {
    try scalanative.NIRCompiler(_.compile(source))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: ${ex.getMessage}", ex)
    }
  }

  "The compiler" should "allow returning a reference to object in zone if it doesn't escape the zone" in nativeCompilation(
    """
      |import scala.language.experimental.captureChecking
      |import scala.scalanative.memory.SafeZone
      |import scala.scalanative.runtime.SafeZoneAllocator.allocate
      |
      |class A(v: Int = 0)
      |
      |def test(): Unit = {
      |  SafeZone { sz0 ?=>
      |    val a = SafeZone { sz1 ?=> 
      |      val a0 = allocate(sz0, new A(0))
      |      a0
      |    }
      |  }
      |}
      |""".stripMargin
  )

  it should "forbid any reference to object in zone from escaping the zone" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
        |import scala.language.experimental.captureChecking
        |import scala.scalanative.memory.SafeZone
        |import scala.scalanative.runtime.SafeZoneAllocator.allocate
        |
        |class A(v: Int = 0)
        |
        |def test(): Unit = {
        |  SafeZone { sz0 ?=>
        |    val a = SafeZone { sz1 ?=> 
        |      allocate(sz1, new A(1))
        |    }
        |  }
        |}
        |""".stripMargin))
    }.getMessage should include("Sealed type variable T cannot  be instantiated to box A^")
  }

  it should "type check when the types capture zones correctly" in nativeCompilation(
    """
      |import scala.language.experimental.captureChecking
      |import scala.scalanative.memory.SafeZone
      |import scala.scalanative.runtime.SafeZoneAllocator.allocate
      |
      |class A(v: Int = 0)
      |class B(a: A^) {}
      |class C(a0: A^, a1: A^)
      |
      |def test(): Unit = {
      |  SafeZone { sz ?=>
      |    val a: A^{sz} = allocate(sz, new A(0)) 
      |    val ary: Array[A]^{sz} = allocate(sz, new Array[A](10)) 
      |
      |    val aInHeap: A^ = new A(0)
      |    val b: B^{sz, aInHeap} = allocate(sz, new B(aInHeap)) 
      |
      |    val aInZone: A^ = allocate(sz, new A(0))
      |    val c: C^{sz, aInZone, aInHeap} = allocate(sz, new C(aInZone, aInHeap)) 
      |  }
      |}
      |""".stripMargin
  )

  it should "not type check when the types don't capture zones correctly" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
        |import scala.language.experimental.captureChecking
        |import scala.scalanative.memory.SafeZone
        |import scala.scalanative.runtime.SafeZoneAllocator.allocate
        |
        |class A (v: Int = 0)
        |class B (a: A^)
        |
        |def test(): Unit = {
        |  SafeZone { sz ?=>
        |    val a: A^{sz} = allocate(sz, new A(0)) 
        |    val ary: Array[A]^{sz} = allocate(sz, new Array[A](10)) 
        |
        |    val aInHeap: A^ = new A(0)
        |    val b: B^{sz} = allocate(sz, new B(aInHeap)) 
        |  }
        |}
        |
        |""".stripMargin))
    }.getMessage should include("Found:    B{val a: A^{aInHeap}}^{aInHeap, sz}")
  }
}
