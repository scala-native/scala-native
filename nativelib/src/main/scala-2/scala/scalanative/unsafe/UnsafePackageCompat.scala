package scala.scalanative.unsafe
import scala.language.experimental.macros

private[scalanative] trait UnsafePackageCompat { self =>

  /** Heap allocate and zero-initialize a value using current implicit
   *  allocator.
   */
  def alloc[T]()(implicit tag: Tag[T], z: Zone): Ptr[T] =
    macro MacroImpl.allocSingle[T]

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  def alloc[T](n: CSize)(implicit tag: Tag[T], z: Zone): Ptr[T] =
    macro MacroImpl.allocN[T]

  /** Stack allocate and zero-initialize 1 value of given type */
  def stackalloc[T]()(implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackallocSingle[T]

  /** Stack allocate and zero-initialize n values of given type */
  def stackalloc[T](n: CSize)(implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackallocN[T]
}

private object MacroImpl {
  import scala.reflect.macros.blackbox.Context

  private def alloc1Impl[T: c.WeakTypeTag](
      c: Context
  )(tag: c.Tree, z: c.Tree): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]

    val size, ptr, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag)
          val $ptr    = $z.alloc($size)
          val $rawptr = $runtime.toRawPtr($ptr)
          $runtime.libc.memset($rawptr, 0, $size)
          $ptr.asInstanceOf[Ptr[$T]]
        }"""
  }

  def allocSingle[T: c.WeakTypeTag](
      c: Context
  )()(tag: c.Tree, z: c.Tree): c.Tree = alloc1Impl(c)(tag, z)

  def allocN[T: c.WeakTypeTag](
      c: Context
  )(n: c.Tree)(tag: c.Tree, z: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val size, ptr, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag) * $n
          val $ptr    = $z.alloc($size)
          val $rawptr = $runtime.toRawPtr($ptr)
          $runtime.libc.memset($rawptr, 0, $size)
          $ptr.asInstanceOf[Ptr[$T]]
        }"""
  }

  def stackallocSingle[T: c.WeakTypeTag](c: Context)()(tag: c.Tree): c.Tree =
    stackalloc1Impl(c)(tag)

  private def stackalloc1Impl[T: c.WeakTypeTag](
      c: Context
  )(tag: c.Tree): c.Tree = {

    import c.universe._

    val T = weakTypeOf[T]

    val size, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag)
          val $rawptr = $runtime.Intrinsics.stackalloc($size)
          $runtime.libc.memset($rawptr, 0, $size)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }

  def stackallocN[T: c.WeakTypeTag](
      c: Context
  )(n: c.Tree)(tag: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val size, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag) * $n
          val $rawptr = $runtime.Intrinsics.stackalloc($size)
          $runtime.libc.memset($rawptr, 0, $size)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }
}
