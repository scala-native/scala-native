package scala.scalanative
package native

/** Let optimizer assume that given method is pure.
  * This means that repetitive calls to the same method
  * with the same arguments are allowed to be eliminated.
  * Additionally, if method result to given method is not used
  * then the call can be elided altogether.
  */
final class pure extends scala.annotation.StaticAnnotation
