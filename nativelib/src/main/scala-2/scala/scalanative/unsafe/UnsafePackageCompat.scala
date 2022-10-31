package scala.scalanative.unsafe
import scala.language.experimental.macros

private[scalanative] trait UnsafePackageCompat { self =>

  /** Heap allocate and zero-initialize a value using current implicit
   *  allocator.
   */
  @deprecated(
    "In Scala 3 alloc[T](n) can be confused with alloc[T].apply(n) leading to runtime errors, use alloc[T]() instead",
    since = "0.4.3"
  )
  def alloc[T](implicit tag: Tag[T], z: Zone): Ptr[T] =
    macro MacroImpl.alloc1[T]

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

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator. This method takes argument of type `CSSize` for easier interop,
   *  but it' always converted into `CSize`
   */
  @deprecated(
    "alloc with signed type is deprecated, convert size to unsigned value",
    "0.4.0"
  )
  def alloc[T](n: CSSize)(implicit tag: Tag[T], z: Zone): Ptr[T] =
    macro MacroImpl.allocN[T]

  /** Stack allocate a value of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized.
   */
  @deprecated(
    "In Scala 3 alloc[T](n) can be confused with alloc[T].apply(n) leading to runtime errors, use alloc[T]() instead",
    since = "0.4.3"
  )
  def stackalloc[T](implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackalloc1[T]

  /** Stack allocate a value of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized.
   */
  def stackalloc[T]()(implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackallocSingle[T]

  /** Stack allocate n values of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized.
   */
  def stackalloc[T](n: CSize)(implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackallocN[T]

  /** Stack allocate n values of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized. This method takes
   *  argument of type `CSSize` for easier interop, but it's always converted
   *  into `CSize`
   */
  @deprecated(
    "alloc with signed type is deprecated, convert size to unsigned value",
    "0.4.0"
  )
  def stackalloc[T](n: CSSize)(implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackallocN[T]
}

private object MacroImpl {
  import scala.reflect.macros.blackbox.Context

  def alloc1[T: c.WeakTypeTag](c: Context)(tag: c.Tree, z: c.Tree): c.Tree = {
    c.warning(
      c.enclosingPosition,
      s"Scala Native method `alloc[T]` is deprecated, " +
        "in Scala 3 `alloc[T](n)` can be interpreted as " +
        "`alloc[T].apply(n)` leading to runtime errors, " +
        "use `alloc[T]()` instead "
    )
    alloc1Impl(c)(tag, z)
  }

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
          import _root_.scala.scalanative.unsigned.UnsignedRichLong
          val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag) * $n
          val $ptr    = $z.alloc($size)
          val $rawptr = $runtime.toRawPtr($ptr)
          $runtime.libc.memset($rawptr, 0, $size)
          $ptr.asInstanceOf[Ptr[$T]]
        }"""
  }

  def stackallocSingle[T: c.WeakTypeTag](c: Context)()(tag: c.Tree): c.Tree =
    stackalloc1Impl(c)(tag)

  def stackalloc1[T: c.WeakTypeTag](c: Context)(tag: c.Tree): c.Tree = {
    c.warning(
      c.enclosingPosition,
      s"Scala Native method `stackalloc[T]` is deprecated, " +
        "in Scala 3 `stackalloc[T](n)` can be interpreted as " +
        "`stackalloc[T].apply(n)` leading to runtime errors, " +
        "use `stackalloc[T]()` instead "
    )
    stackalloc1Impl(c)(tag)
  }

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
          import _root_.scala.scalanative.unsigned.UnsignedRichLong
          val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag) * $n
          val $rawptr = $runtime.Intrinsics.stackalloc($size)
          $runtime.libc.memset($rawptr, 0, $size)
          $runtime.fromRawPtr[$T]($rawptr)
        }"""
  }
}
