package scala.issues

import org.junit.Assert._
import org.junit.Test

/* Since scala/scala3#27294 (Scala 3.10.2), a catch case `case e: (A | B)` of
 * disjoint exception types stays a catch case instead of going through the
 * pattern matcher. Its type test must accept an instance of each alternative,
 * not of their erased common superclass.
 */
class UnionCatchTest {
  class FirstException extends Exception
  class SecondException extends RuntimeException
  class OtherException extends RuntimeException

  private def classify(e: Throwable): String =
    try throw e
    catch {
      case ex: (FirstException | SecondException) =>
        s"union: ${ex.getClass.getSimpleName}"
      case _: IllegalStateException | _: IllegalArgumentException =>
        "alternatives"
      case ex: Exception => s"other: ${ex.getClass.getSimpleName}"
    }

  @Test def unionCatchCaseAcceptsEachAlternative(): Unit = {
    assertEquals("union: FirstException", classify(new FirstException))
    assertEquals("union: SecondException", classify(new SecondException))
  }

  @Test def unionCatchCaseRejectsOtherSubclassesOfTheCommonSuperclass()
      : Unit = {
    assertEquals("other: OtherException", classify(new OtherException))
    assertEquals(
      "other: UnsupportedOperationException",
      classify(new UnsupportedOperationException)
    )
  }

  @Test def alternativesOfTypedPatterns(): Unit = {
    assertEquals("alternatives", classify(new IllegalStateException))
    assertEquals("alternatives", classify(new IllegalArgumentException))
  }
}
