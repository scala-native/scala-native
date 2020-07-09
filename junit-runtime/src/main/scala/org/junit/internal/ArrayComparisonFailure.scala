/*
 * Ported from https://github.com/junit-team/junit
 */
package org.junit.internal

object ArrayComparisonFailure

class ArrayComparisonFailure(message: String, cause: AssertionError, index: Int)
    extends AssertionError(message, cause) {

  private var fIndices: List[Int] = index :: Nil

  def addDimension(index: Int): Unit = {
    fIndices = index :: fIndices
  }

  override def getMessage: String = {
    val msg = if (message != null) message else ""
    val indices =
      if (fIndices == null) s"[$index]" // see scala-js/scala-js#3148
      else fIndices.map(index => s"[$index]").mkString
    val causeMessage = getCause.getMessage
    s"${msg}arrays first differed at element $indices; $causeMessage"
  }

  override def toString: String = getMessage
}
