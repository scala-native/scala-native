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
  def alloc[T](n: Int)(implicit z: Zone): Ptr[T] = macro MacroImpl.allocN[T]

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  def alloc[T](n: CSize)(implicit z: Zone): Ptr[T] =
    macro MacroImpl.allocNUnsigned[T]

  /** Stack allocate and zero-initialize 1 value of given type */
  def stackalloc[T](): Ptr[T] = macro MacroImpl.stackallocSingle[T]

  /** Stack allocate and zero-initialize n values of given type */
  def stackalloc[T](n: Int): Ptr[T] = macro MacroImpl.stackallocN[T]

  /** Stack allocate and zero-initialize n values of given type */
  def stackalloc[T](n: CSize): Ptr[T] = macro MacroImpl.stackallocNUnsigned[T]

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

    val elemSize, size, ptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"
    val unsignedOf = q"$runtime.Intrinsics.unsignedOf"

    q"""{
          val $elemSize = $runtime.Intrinsics.sizeOf[$T]
          val $size = 
            $unsignedOf($elemSize) * $unsignedOf(${validateSize(c)(n)})
          val $ptr     = $z.alloc($size)
          $runtime.libc.memset($ptr, 0, $size)
          $ptr.asInstanceOf[Ptr[$T]]
        }"""
  }

  def allocNUnsigned[T: c.WeakTypeTag](
      c: Context
  )(n: c.Tree)(z: c.Tree): c.Tree = {
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

  private def validateSize(c: Context)(size: c.Tree): c.Tree = {
    import c.universe._
    size match {
      case lit @ Literal(Constant(size: Int)) =>
        if (size == 0)
          c.error(c.enclosingPosition, "Allocatation of size 0 is fruitless")
        else if (size < 0)
          c.error(
            c.enclosingPosition,
            "Cannot allocate memory of negative size"
          )
        lit
      case expr =>
        q"""{
          _root_.scala.Predef.require($expr > 0, "Cannot allocate memory of negative size")
          $expr
        }
        """
    }
  }

  def stackallocN[T: c.WeakTypeTag](c: Context)(n: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val elemSize, elements, size, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"
    val toRawSize = q"$runtime.Intrinsics.castIntToRawSizeUnsigned"
    val toInt = q"$runtime.Intrinsics.castRawSizeToInt"

    q"""{
          val $elemSize = $runtime.Intrinsics.sizeOf[$T]
          val $elements = $toRawSize(${validateSize(c)(n)})
          val $size = $toRawSize($toInt($elemSize) * $n)
          val $rawptr  = $runtime.Intrinsics.stackalloc($elemSize, $elements)
          $runtime.libc.memset($rawptr, 0, $size)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }

  def stackallocNUnsigned[T: c.WeakTypeTag](c: Context)(n: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val elemSize, elements, size, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"
    val toRawSize = q"$runtime.Intrinsics.castIntToRawSizeUnsigned"
    val toInt = q"$runtime.Intrinsics.castRawSizeToInt"

    q"""{
          val $elemSize = $runtime.Intrinsics.sizeOf[$T]
          val $elements = $runtime.toRawSize($n)
          val $size = $toRawSize($toInt($elemSize) * $toInt($elements))
          val $rawptr  = $runtime.Intrinsics.stackalloc($elemSize, $elements)
          $runtime.libc.memset($rawptr, 0, $size)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }
}
