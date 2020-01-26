package scala.scalanative.cpp
import scalanative.unsafe._

package object ios {
    type Streamsize = CLongInt
    type NativeObject = Ptr[CChar]
    type Bitmask = CLongInt
}

