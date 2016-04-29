package scala.scalanative

import native.Ptr

package object runtime {
  def undefined: Nothing = throw new UndefinedBehaviorError

  def init(argc: Int, argv: Ptr[Ptr[Byte]]): RefArray = ???
}
