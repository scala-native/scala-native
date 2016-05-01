package scala.scalanative
package runtime

import native.{struct, Ptr}

@struct class Type(val id: Int, val name: String)

object Type {
  def get(obj: Object): Ptr[Type] = undefined
}
