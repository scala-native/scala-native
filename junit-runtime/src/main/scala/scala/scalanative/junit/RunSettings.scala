package scala.scalanative
package junit

// Ported from Scala.js

import scala.util.Try
import RunSettings._

private[junit] final class RunSettings(
    val color: Boolean,
    decodeScalaNames: Boolean,
    val verbosity: Verbosity,
    val logAssert: Boolean,
    val logExceptionClass: Boolean
) {
  def decodeName(name: String): String = {
    if (decodeScalaNames)
      Try(scala.reflect.NameTransformer.decode(name)).getOrElse(name)
    else name
  }
}

object RunSettings {
  sealed abstract class Verbosity(val ordinal: Int)
  object Verbosity {
    case object Terse extends Verbosity(0)
    case object RunFinished extends Verbosity(1)
    case object Started extends Verbosity(2)
    case object TestFinished extends Verbosity(3)

    def ofOrdinal(v: Int): Verbosity = v match {
      case 0 => Terse
      case 1 => RunFinished
      case 2 => Started
      case 3 => TestFinished
      case n => throw new IllegalArgumentException(s"--verbosity=$n")
    }
  }
}
