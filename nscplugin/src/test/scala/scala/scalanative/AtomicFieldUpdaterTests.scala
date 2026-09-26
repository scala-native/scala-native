package scala.scalanative

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.nir._

class AtomicFieldUpdaterTests {
  private def compile(source: String): Unit =
    try NIRCompiler(_.compile(source.stripMargin))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: $ex")
    }

  private def compilationError(source: String): String =
    assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(source.stripMargin))
    ).getMessage()

  @Test def bindsAllUpdaterKinds(): Unit = compile(
    """|
       |import java.util.concurrent.atomic._
       |
       |class Target {
       |  @volatile var intValue: Int = 0
       |  @volatile var longValue: Long = 0L
       |  @volatile var refValue: String = null
       |
       |  val ints = AtomicIntegerFieldUpdater.newUpdater(classOf[Target], "intValue")
       |  val longs = AtomicLongFieldUpdater.newUpdater(classOf[Target], "longValue")
       |  val refs = AtomicReferenceFieldUpdater.newUpdater(
       |    classOf[Target], classOf[String], "refValue"
       |  )
       |}
       |"""
  )

  @Test def acceptsStaticallyTypedGetClassReceivers(): Unit = compile(
    """|
       |import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
       |
       |class Target {
       |  @volatile protected var value: Int = 0
       |  val fromThis = AtomicIntegerFieldUpdater.newUpdater(this.getClass, "value")
       |  def fromVariable = {
       |    val target: Target = this
       |    AtomicIntegerFieldUpdater.newUpdater(target.getClass, "value")
       |  }
       |}
       |class Child extends Target {
       |  val fromSuper = AtomicIntegerFieldUpdater.newUpdater(super.getClass, "value")
       |}
       |"""
  )

  @Test def acceptsPrivateFieldsFromTheirOwner(): Unit = compile(
    """|
       |import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
       |
       |class Target {
       |  @volatile private var value: AnyRef = null
       |  def current: AnyRef = value
       |  val updater = AtomicReferenceFieldUpdater.newUpdater(
       |    classOf[Target], classOf[AnyRef], "value"
       |  )
       |}
       |"""
  )

  @Test def acceptsWildcardedReferenceFields(): Unit = compile(
    """|
       |import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
       |
       |class Node[A <: AnyRef] {
       |  @volatile var next: Node[A] = null
       |}
       |object Node {
       |  val updater = AtomicReferenceFieldUpdater.newUpdater[Node[?], Node[?]](
       |    classOf[Node[?]], classOf[Node[?]], "next"
       |  )
       |}
       |"""
  )

  @Test def linksUpdaterFactory(): Unit =
    compileAndLoad(
      "UpdaterFactory.scala" ->
        """|
           |import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
           |class Target { @volatile var value = 0 }
           |object UpdaterFactory {
           |  val updater = AtomicIntegerFieldUpdater.newUpdater(
           |    classOf[Target], "value"
           |  )
           |}
           |""".stripMargin
    )(_ => ())

  @Test def emitsFacadeCallInsteadOfUpdaterSubclass(): Unit =
    compileAndLoad(
      "Updater.scala" ->
        """|
           |import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
           |class Target {
           |  @volatile var value: Int = 0
           |  val updater = AtomicIntegerFieldUpdater.newUpdater(classOf[Target], "value")
           |}
           |""".stripMargin
    ) { defns =>
      val bindings = defns.collect {
        case cls: Defn.Class
            if cls.name.show.contains("$AtomicFieldBinding$") =>
          cls
      }
      assertEquals(0, bindings.size)
      assertFalse(
        defns
          .collect { case cls: Defn.Class => cls }
          .exists(
            _.parent.exists(
              _.show.startsWith("java.util.concurrent.atomic.Atomic")
            )
          )
      )
    }

  @Test def rejectsInvalidDeclarations(): Unit = {
    def errorFor(field: String, updater: String) = compilationError(
      s"""|
          |import java.util.concurrent.atomic._
          |class Target {
          |  $field
          |  val updater = $updater
          |}
          |"""
    )

    val cases = Seq(
      errorFor(
        "@volatile var value: Int = 0",
        "AtomicIntegerFieldUpdater.newUpdater(classOf[Target], \"missing\")"
      ) -> "does not contain field missing",
      errorFor(
        "var value: Int = 0",
        "AtomicIntegerFieldUpdater.newUpdater(classOf[Target], \"value\")"
      ) -> "requires a volatile mutable field value",
      errorFor(
        "@volatile val value: Int = 0",
        "AtomicIntegerFieldUpdater.newUpdater(classOf[Target], \"value\")"
      ) -> "volatile",
      errorFor(
        "@volatile var value: Long = 0L",
        "AtomicIntegerFieldUpdater.newUpdater(classOf[Target], \"value\")"
      ) -> "type does not match field value",
      errorFor(
        "@volatile var value: String = null",
        "AtomicReferenceFieldUpdater.newUpdater(classOf[Target], classOf[Object], \"value\")"
      ) -> "type does not match field value",
      errorFor(
        "@volatile var value: Int = 0; val name = \"value\"",
        "AtomicIntegerFieldUpdater.newUpdater(classOf[Target], name)"
      ) -> "field name must be a literal string",
      errorFor(
        "@volatile var value: String = null; val cls = classOf[String]",
        "AtomicReferenceFieldUpdater.newUpdater(classOf[Target], cls, \"value\")"
      ) -> "value class must be a literal",
      errorFor(
        "@volatile var value: Int = 0; val cls = classOf[Target]",
        "AtomicIntegerFieldUpdater.newUpdater(cls, \"value\")"
      ) -> "class must be a literal classOf[T] expression"
    )
    cases.foreach {
      case (actual, expected) =>
        assertTrue(actual, actual.contains(expected))
    }
  }

  @Test def rejectsInaccessibleField(): Unit = {
    val error = compilationError(
      """|
         |import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
         |class Target { @volatile private var value: Int = 0 }
         |object Use {
         |  val updater = AtomicIntegerFieldUpdater.newUpdater(classOf[Target], "value")
         |}
         |"""
    )
    assertFalse(error.isEmpty)
  }

  @Test def rejectsStaticField(): Unit = {
    if (scala.util.Properties.versionNumberString.startsWith("3.")) {
      val error = compilationError(
        """|
           |import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
           |import scala.annotation.static
           |class Target
           |object Target {
           |  @static @volatile var value: Int = 0
           |  val updater = AtomicIntegerFieldUpdater.newUpdater(
           |    classOf[Target], "value"
           |  )
           |}
           |"""
      )
      assertTrue(error, error.contains("cannot target static field value"))
    }
  }
}
