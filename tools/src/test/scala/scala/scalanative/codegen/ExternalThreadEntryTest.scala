package scala.scalanative.codegen

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.{build, nir}

class ExternalThreadEntryTest extends CodeGenSpec {
  import ExternalThreadEntry._

  private val sources = Map(
    "Main.scala" ->
      """|import scala.scalanative.unsafe._
         |object Main {
         |  @exported("test_external_branch")
         |  def branch(value: Int): Int = if (value < 0) -value else value + 1
         |  @exported("test_external_throw")
         |  def throwing(): Int = throw new IllegalStateException("boundary")
         |  @exported("test_external_boxed")
         |  def boxed(value: Ptr[Byte]): Ptr[Byte] = value
         |  @exported("test_external_pointer")
         |  def pointer(): Ptr[Byte] = CFuncPtr.toPtr(
         |    CFuncPtr1.fromScalaFunction((value: Int) => branch(value))
         |  ).asInstanceOf[Ptr[Byte]]
         |  def main(args: Array[String]): Unit = {
         |    println(branch(Cold.value))
         |    println(pointer())
         |  }
         |}
         |object Cold {
         |  @exportAccessors("test_external_get", "test_external_set")
         |  var value: Int = new Array[Int](4).length
         |}
         |""".stripMargin
  )

  private def calls(defn: nir.Defn.Define): Seq[nir.Global] =
    defn.insts.collect {
      case nir.Inst.Let(_, nir.Op.Call(_, nir.Val.Global(name, _), _), _) =>
        name
    }

  private def exported(
      defs: Seq[nir.Defn],
      symbol: String
  ): nir.Defn.Define = defs
    .collectFirst {
      case defn: nir.Defn.Define
          if defn.name.sig.unmangled == nir.Sig.Extern(symbol) =>
        defn
    }
    .getOrElse(throw new AssertionError(s"Missing exported entry $symbol"))

  private def assertGuarded(defn: nir.Defn.Define): Unit = {
    assertEquals(
      s"Admission must precede calls in ${defn.name}",
      EnterManagedName,
      calls(defn).head
    )
    defn.insts.zipWithIndex.foreach {
      case (_: nir.Inst.Ret, index) =>
        assertTrue(s"Missing return restoration in ${defn.name}", index > 0)
        defn.insts(index - 1) match {
          case nir.Inst.Let(_, nir.Op.Call(_, nir.Val.Global(name, _), _), _) =>
            assertEquals(LeaveManagedName, name)
          case other => fail(s"Return restoration must precede ret: $other")
        }
      case _ => ()
    }
    assertTrue(
      s"Missing exceptional restoration in ${defn.name}",
      calls(defn).contains(LeaveManagedAndThrowName)
    )
  }

  @Test def automaticAdmissionAndRestorationWithBothExceptionBackends()
      : Unit = {
    for (cpp <- Seq(false, true)) {
      lowered(
        "Main",
        sources,
        _.withOptimize(false)
          .withMultithreading(true)
          .withCppOptions(
            Seq(if (cpp) "-fcxx-exceptions" else "-fno-cxx-exceptions")
          )
      ) { defs =>
        for (name <- Seq(
              "test_external_branch",
              "test_external_throw",
              "test_external_boxed",
              "test_external_pointer",
              "test_external_get",
              "test_external_set"
            )) {
          assertGuarded(exported(defs, name))
        }
        val functionPointers = defs.collect {
          case defn: nir.Defn.Define
              if defn.name.sig.unmangled == nir.Sig.Generated(
                "$extern$forwarder"
              ) &&
                defn.name.top.id.startsWith("Main") =>
            defn
        }
        assertFalse(
          "Missing Scala function-pointer wrapper",
          functionPointers.isEmpty
        )
        functionPointers.foreach(assertGuarded)
        val boxing = calls(exported(defs, "test_external_boxed"))
        assertTrue(
          "Argument boxing was not exercised",
          boxing.exists {
            _.asInstanceOf[nir.Global.Member].sig.unmangled match {
              case nir.Sig.Method(name, _, _) => name.startsWith("boxTo")
              case _                          => false
            }
          }
        )
        val getter = calls(exported(defs, "test_external_get"))
        assertTrue(
          "Cold module loading was not exercised",
          getter.exists(_.top.id == "Cold$")
        )
        val thrown = calls(exported(defs, "test_external_throw"))
        assertEquals(
          "Catch admission hook follows the selected exception ABI",
          cpp,
          thrown.contains(Lower.ExceptionOnCatchName)
        )
        val bootstrap = exported(defs, "main")
        assertFalse(calls(bootstrap).contains(EnterManagedName))
        assertFalse(calls(bootstrap).contains(Lower.GCYieldName))
        val runtimeHooks = defs.collect {
          case defn: nir.Defn.Define
              if defn.attrs.isExtern &&
                (defn.name.top.id.startsWith("scala.scalanative.runtime.") ||
                defn.name.top.id.startsWith(
                  "java.lang.ref.WeakReferenceRegistry$"
                )) =>
            defn
        }
        assertFalse(
          "Runtime hook exemptions were not exercised",
          runtimeHooks.isEmpty
        )
        runtimeHooks.foreach { defn =>
          assertFalse(
            s"Runtime hook must not enter: ${defn.name}",
            calls(defn).contains(EnterManagedName)
          )
        }
      }
    }
  }

  @Test def libraryBootstrapDoesNotPollAfterReleasingManagedState(): Unit =
    for (gc <- Seq(build.GC.immix, build.GC.commix)) {
      lowered(
        "Main",
        sources,
        _.withOptimize(false)
          .withBuildTarget(build.BuildTarget.libraryStatic)
          .withGC(gc)
          .withMultithreading(true),
        library = true
      ) { defs =>
        val initializer = exported(defs, "ScalaNativeInit")
        assertTrue(calls(initializer).contains(ThreadBootstrapCompleteName))
        assertFalse(calls(initializer).contains(EnterManagedName))
        assertFalse(calls(initializer).contains(Lower.GCYieldName))
      }
    }

  @Test def bootstrapAndInternalExportsStayExempt(): Unit = {
    val signature = nir.Type.Function(Nil, nir.Type.Unit)
    def definition(owner: String, symbol: String) = nir.Defn.Define(
      nir.Attrs.None.withIsExtern(true),
      nir.Global.Top(owner).member(nir.Sig.Extern(symbol)),
      signature,
      Seq.empty
    )(nir.SourcePosition.NoPosition)
    for (symbol <- Seq("ScalaNativeInit", "main")) {
      val global = Extern(symbol)
      assertFalse(
        symbol,
        shouldGuard(definition(global.owner.id, symbol))
      )
      assertTrue(symbol, shouldGuard(definition("User$", symbol)))
    }
    for (global <- Seq(AdoptThreadName, ReleaseThreadName)) {
      global.sig.unmangled match {
        case nir.Sig.Extern(symbol) =>
          assertFalse(
            symbol,
            shouldGuard(definition(global.owner.id, symbol))
          )
          assertTrue(symbol, shouldGuard(definition("User$", symbol)))
        case other => fail(s"Expected extern signature: $other")
      }
    }
    assertFalse(
      shouldGuard(definition("scala.scalanative.runtime.GC$", "hook"))
    )
    assertFalse(
      shouldGuard(
        definition("java.lang.ref.WeakReferenceRegistry$", "hook")
      )
    )
    assertTrue(shouldGuard(definition("User$", "user_callback")))
  }
}
