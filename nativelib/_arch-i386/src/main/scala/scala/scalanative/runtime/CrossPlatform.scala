package scala.scalanative
package runtime

object CrossPlatform {
  @inline final def cross3264[A, B](thirtyTwo: => A, sixtyFour: => B): Cross3264[A, B] = {
    thirtyTwo
  }

  type Cross3264[ThirtyTwo, SixtyFour] = ThirtyTwo
}