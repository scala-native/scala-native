/*
 * Ported from https://github.com/junit-team/junit
 */
package org.junit.internal

import org.hamcrest.{Description, Matcher, SelfDescribing, StringDescription}

class AssumptionViolatedException protected (
    fAssumption: String,
    fValueMatcher: Boolean,
    fMatcher: Matcher[?],
    fValue: AnyRef
) extends RuntimeException
    with SelfDescribing {

  override def getMessage: String =
    StringDescription.asString(this)

  def describeTo(description: Description): Unit = {
    if (fAssumption != null)
      description.appendText(fAssumption)

    if (fValueMatcher) {
      if (fAssumption != null)
        description.appendText(": ")

      description.appendText("got: ")
      description.appendValue(fValue)

      if (fMatcher != null) {
        description.appendText(", expected: ")
        description.appendDescriptionOf(fMatcher)
      }
    }
  }
}
