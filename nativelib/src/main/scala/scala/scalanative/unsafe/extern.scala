package scala.scalanative
package unsafe

/** An annotation internally used in NativePlugin used to mark externally-defined members.
 *  Is applied only in PrepNativeInterop phase
 */
sealed trait extern extends scala.annotation.StaticAnnotation
