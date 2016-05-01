package scala.scalanative

import native.Ptr

package object runtime {
  /** Used as a stub right hand of intrinsified methods. */
  def undefined: Nothing = throw new UndefinedBehaviorError

  /** Initialize runtime with given arguments and return the rest as Java-style array. */
  def init(argc: Int, argv: Ptr[Ptr[Byte]]): RefArray = null
}
