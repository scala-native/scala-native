package scala.scalanative
package junit

// Ported from Scala.js

import scala.util.Try

private[junit] final class RunSettings(
    val color: Boolean,
    decodeScalaNames: Boolean,
    val verbose: Boolean,
    val logAssert: Boolean,
    val notLogExceptionClass: Boolean
) {
  def decodeName(name: String): String = {
    if (decodeScalaNames)
      Try(scala.reflect.NameTransformer.decode(name)).getOrElse(name)
    else name
  }
}
