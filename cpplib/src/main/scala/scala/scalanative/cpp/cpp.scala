package scala.scalanative
import scalanative.unsafe._

package object cpp {
    type NativeObject = Ptr[CChar]
    val NullObj: NativeObject = 0.asInstanceOf[NativeObject]
}

