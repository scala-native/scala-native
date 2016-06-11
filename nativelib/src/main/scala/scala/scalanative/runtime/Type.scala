package scala.scalanative
package runtime

import native.{struct, Ptr}

@struct class Type(val id: Int, val name: String)
