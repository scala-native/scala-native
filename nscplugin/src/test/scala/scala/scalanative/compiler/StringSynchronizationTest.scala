package scala.scalanative.compiler

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.NIRCompiler
import scala.scalanative.api.CompilationFailedException

class StringSynchronizationTest {
  private def rejects(receiver: String): Unit = {
    val error = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            s"object Test { def run(): Unit = $receiver.synchronized(()) }"
          )
        )
    )
    assertTrue(
      error.getMessage.contains(
        "Synchronization on String instances is not supported"
      )
    )
  }

  @Test def literal(): Unit = rejects("\"literal\"")
  @Test def allocated(): Unit = rejects("new String(Array('a'))")
  @Test def parameter(): Unit = {
    val error = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            "object Test { def run(s: String): Unit = s.synchronized(()) }"
          )
        )
    )
    assertTrue(
      error.getMessage.contains(
        "Synchronization on String instances is not supported"
      )
    )
  }

  @Test def erasedReceiverStillCompiles(): Unit = NIRCompiler(
    _.compile(
      """object Test {
        |  def run(lock: AnyRef): Unit = lock.synchronized(())
        |  def generic[T <: AnyRef](lock: T): Unit = lock.synchronized(())
        |  def widened(): Unit = run("literal")
        |}""".stripMargin
    )
  )
}
