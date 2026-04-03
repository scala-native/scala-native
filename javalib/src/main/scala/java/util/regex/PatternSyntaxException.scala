/*
 * Derived from Scala.js / scala-wasm (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 */

package java.util.regex

class PatternSyntaxException(desc: String, regex: String, index: Int)
    extends IllegalArgumentException {

  def getIndex(): Int = index

  def getDescription(): String = desc

  def getPattern(): String = regex

  override def getMessage(): String = {
    val idx = index
    val re = regex

    val indexHint = if (idx < 0) "" else " near index " + idx
    val base = desc + indexHint + "\n" + re

    if (idx >= 0 && re != null && idx < re.length())
      base + "\n" + " ".repeat(idx) + "^"
    else
      base
  }
}
