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

    val rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"

    q"""{
          val $rawptr  = $runtime.Intrinsics.stackalloc[$T]()
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }

  private def validateSize(c: Context)(size: c.Tree): c.Tree = {
    import c.universe._
    size match {
      case lit @ Literal(Constant(size: Int)) =>
        if (size == 0)
          c.error(c.enclosingPosition, "Allocation of size 0 is fruitless")
        else if (size < 0)
          c.error(
            c.enclosingPosition,
            "Cannot allocate memory of negative size"
          )
        lit
      case expr =>
        val size = TermName(c.freshName())
        q"""{
          val $size = $expr
          if($size < 0) throw new java.lang.IllegalArgumentException("Cannot allocate memory of negative size")
          $size
        }
        """
    }
  }

  def stackallocN[T: c.WeakTypeTag](c: Context)(n: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val elements, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"
    val toRawSize = q"$runtime.Intrinsics.castIntToRawSizeUnsigned"

    q"""{
          val $rawptr  = $runtime.Intrinsics.stackalloc[$T](
              $toRawSize(${validateSize(c)(n)})
            )
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }

  def stackallocNUnsigned[T: c.WeakTypeTag](c: Context)(n: c.Tree): c.Tree = {
    import c.universe._

    val T = weakTypeOf[T]

    val elements, rawptr = TermName(c.freshName())

    val runtime = q"_root_.scala.scalanative.runtime"
    val toRawSize = q"$runtime.Intrinsics.castIntToRawSizeUnsigned"
    val toInt = q"$runtime.Intrinsics.castRawSizeToInt"

    q"""{
          val $elements = $runtime.toRawSize($n)
          val $rawptr  = $runtime.Intrinsics.stackalloc[$T]($elements)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }
}
