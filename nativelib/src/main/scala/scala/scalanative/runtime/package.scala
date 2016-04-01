package scala.scalanative

package object runtime {
  def undefined: Nothing = throw new UndefinedBehaviorError
}


