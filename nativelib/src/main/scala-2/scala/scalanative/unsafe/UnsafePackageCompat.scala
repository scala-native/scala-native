package scala.scalanative.unsafe
import scala.language.experimental.macros

private[scalanative] trait UnsafePackageCompat { self =>

  /** The Scala equivalent of C 'ssizeof', but always returns 32-bit integer */
  def sizeOf[T]: Int = macro MacroImpl.sizeOf[T]

  /** The C 'sizeof' operator. */
  def sizeof[T]: CSize = macro MacroImpl.sizeof[T]

  /** The C 'sizeof' operator. */
  def ssizeof[T]: CSSize = macro MacroImpl.ssizeof[T]

  /** The Scala equivalent of C 'alignmentof', but always returns 32-bit integer
   */
  def alignmentOf[T]: Int = macro MacroImpl.alignmentOf[T]

  /** The C 'alignmentof' operator. */
  def alignmentof[T]: CSize = macro MacroImpl.alignmentof[T]

  /** Heap allocate and zero-initialize a value using current implicit
   *  allocator.
   */
  def alloc[T]()(implicit z: Zone): Ptr[T] = macro MacroImpl.allocSingle[T]

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  def alloc[T](n: CSize)(implicit z: Zone): Ptr[T] = macro MacroImpl.allocN[T]

  /** Stack allocate and zero-initialize 1 value of given type */
  def stackalloc[T](): Ptr[T] = macro MacroImpl.stackallocSingle[T]

  /** Stack allocate and zero-initialize n values of given type */
  def stackalloc[T](n: CSize): Ptr[T] = macro MacroImpl.stackallocN[T]
}

private object MacroImpl {
  import scala.reflect.macros.blackbox.Context

  def alignmentOf[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]
    val runtime = q"_root_.scala.scalanative.runtime"
    q"$runtime.Intrinsics.castRawSizeToInt($runtime.Intrinsics.alignmentOf[$T])"
  }

  def alignmentof[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]
    val runtime = q"_root_.scala.scalanative.runtime"
    q"$runtime.fromRawUSize($runtime.Intrinsics.alignmentOf[$T])"
  }

  def sizeOf[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]
    val runtime = q"_root_.scala.scalanative.runtime"
    q"$runtime.Intrinsics.castRawSizeToInt($runtime.Intrinsics.sizeOf[$T])"
  }

  def sizeof[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]
    val runtime = q"_root_.scala.scalanative.runtime"
    q"$runtime.fromRawUSize($runtime.Intrinsics.sizeOf[$T])"
  }

  def ssizeof[T: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]
    val runtime = q"_root_.scala.scalanative.runtime"
    q"$runtime.fromRawSize($runtime.Intrinsics.sizeOf[$T])"
  }

  def allocSingle[T: c.WeakTypeTag](c: Context)()(z: c.Tree): c.Tree = {
    import c.universe._
    val T = weakTypeOf[T]

    val size, ptr, rawSize = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $rawSize = $runtime.Intrinsics.sizeOf[$T]
          val $size    = $runtime.fromRawUSize($rawSize)
          val $ptr     = $z.alloc($size)
          $runtime.libc.memset($ptr, 0, $size)
          $ptr.asInstanceOf[Ptr[$T]]
        }"""
  }

  def allocN[T: c.WeakTypeTag](c: Context)(n: c.Tree)(z: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val size, ptr, rawSize = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $rawSize = $runtime.Intrinsics.sizeOf[$T]
          val $size    = $runtime.fromRawUSize($rawSize) * $n
          val $ptr     = $z.alloc($size)
          $runtime.libc.memset($ptr, 0, $size)
          $ptr.asInstanceOf[Ptr[$T]]
        }"""
  }

  def stackallocSingle[T: c.WeakTypeTag](c: Context)(): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val size, rawptr, rawSize = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $rawSize = $runtime.Intrinsics.sizeOf[$T]
          val $rawptr  = $runtime.Intrinsics.stackalloc($rawSize)
          $runtime.libc.memset($rawptr, 0, $rawSize)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }

  def stackallocN[T: c.WeakTypeTag](c: Context)(n: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val size, rawptr, rawSize = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $rawSize = $runtime.Intrinsics.sizeOf[$T]
          val $size    = $runtime.fromRawUSize($rawSize) * $n
          val $rawptr  = $runtime.Intrinsics.stackalloc($size)
          $runtime.libc.memset($rawptr, 0, $runtime.toRawSize($size))
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }
}
