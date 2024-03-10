package scala.scalanative.build

/* Implementation of code santizier  */
sealed abstract class Sanitizer(val name: String)
object Sanitizer {
  case object AddressSanitizer extends Sanitizer("address")
  case object ThreadSanitizer extends Sanitizer("thread")
  case object UndefinedBehaviourSanitizer extends Sanitizer("undefined")
}
