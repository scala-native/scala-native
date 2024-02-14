.. _interop:

Native code interoperability
============================

Scala Native provides an interop layer that makes it easy to interact with
foreign native code. This includes C and other languages that can expose APIs
via C ABI (e.g. C++, D, Rust etc.)

All of the interop APIs discussed here are defined in
``scala.scalanative.unsafe`` package. For brevity, we're going
to refer to that namespace as just ``unsafe``.

Extern objects
--------------

Extern objects are simple wrapper objects that demarcate scopes where methods
are treated as their native C ABI-friendly counterparts. They are
roughly analogous to header files with top-level function declarations in C.

For example, to call C's ``malloc`` one might declare it as following:

.. code-block:: scala

    import scala.scalanative.unsafe._

    @extern
    object libc {
      def malloc(size: CSize): Ptr[Byte] = extern
    }

``extern`` on the right hand side of the method definition signifies
that the body of the method is defined elsewhere in a native library that is
available on the library path (see `Linking with native libraries`_). The
signature of the external function must match the signature of the original C
function (see `Finding the right signature`_).

Finding the right signature
```````````````````````````

To find a correct signature for a given C function one must provide an
equivalent Scala type for each of the arguments:

========================= =========================
C Type                    Scala Type
========================= =========================
``void``                  ``Unit``
``bool``                  ``unsafe.CBool``
``char``                  ``unsafe.CChar``
``signed char``           ``unsafe.CSignedChar``
``unsigned char``         ``unsafe.CUnsignedChar`` [1]_
``short``                 ``unsafe.CShort``
``unsigned short``        ``unsafe.CUnsignedShort`` [1]_
``int``                   ``unsafe.CInt``
``long int``              ``unsafe.CLongInt``
``unsigned int``          ``unsafe.CUnsignedInt`` [1]_
``unsigned long int``     ``unsafe.CUnsignedLongInt`` [1]_
``long``                  ``unsafe.CLong``
``unsigned long``         ``unsafe.CUnsignedLong`` [1]_
``long long``             ``unsafe.CLongLong``
``unsigned long long``    ``unsafe.CUnsignedLongLong`` [1]_
``size_t``                ``unsafe.CSize``
``ssize_t``               ``unsafe.CSSize``
``ptrdiff_t``             ``unsafe.CPtrDiff`` [2]_
``wchar_t``               ``unsafe.CWideChar``
``char16_t``              ``unsafe.CChar16``
``char32_t``              ``unsafe.CChar32``
``float``                 ``unsafe.CFloat``
``double``                ``unsafe.CDouble``
``void*``                 ``unsafe.CVoidPtr`` [2]_
``int*``                  ``unsafe.Ptr[unsafe.CInt]`` [2]_
``char*``                 ``unsafe.CString`` [2]_ [3]_
``int (*)(int)``          ``unsafe.CFuncPtr1[unsafe.CInt, unsafe.CInt]`` [2]_ [4]_
``struct { int x, y; }*`` ``unsafe.Ptr[unsafe.CStruct2[unsafe.CInt, unsafe.CInt]]`` [2]_ [5]_
``struct { int x, y; }``  Not supported
========================= =========================

.. [1] See `Unsigned integer types`_.
.. [2] See `Pointer types`_.
.. [3] See `Byte strings`_.
.. [4] See `Function pointers`_.
.. [5] See `Memory layout types`_.

Linking with native libraries
`````````````````````````````

C compilers typically require to pass an additional ``-l mylib`` flag to
dynamically link with a library. In Scala Native, one can annotate libraries to
link with using the ``@link`` annotation.

.. code-block:: scala

   import scala.scalanative.unsafe._

   @link("mylib")
   @extern
   object mylib {
     def f(): Unit = extern
   }

Whenever any of the members of ``mylib`` object are reachable, the Scala Native
linker will automatically link with the corresponding native library.

As in C, library names are specified without the ``lib`` prefix. For example,
the library `libuv <https://github.com/libuv/libuv>`_  corresponds to
``@link("uv")`` in Scala Native.

It is possible to rename functions using the ``@name`` annotation. Its use is
recommended to enforce the Scala naming conventions in bindings:

.. code-block:: scala

    import scala.scalanative.unsafe._

    @link("uv")
    @extern
    object uv {
      @name("uv_uptime")
      def uptime(result: Ptr[CDouble]): Int = extern
    }

If a library has multiple components, you could split the bindings into separate
objects as it is permitted to use the same ``@link`` annotation more than once.

Variadic functions
``````````````````

Scala Native supports native interoperability with C's variadic argument
list type (i.e. ``va_list``), but not ``...`` varargs. For example ``vprintf``
can be declared as:

.. code-block:: scala

   import scala.scalanative.unsafe._

   @extern
   object mystdio {
     def vprintf(format: CString, args: CVarArgList): CInt = extern
   }

One can wrap a function in a nicer API like:

.. code-block:: scala

   import scala.scalanative.unsafe._

   def myprintf(format: CString, args: CVarArg*): CInt =
     Zone { implicit z =>
       mystdio.vprintf(format, toCVarArgList(args.toSeq))
     }

And then call it just like a regular Scala function:

.. code-block:: scala

   myprintf(c"2 + 3 = %d, 4 + 5 = %d", 2 + 3, 4 + 5)

Exported methods
----------------

When linking Scala Native as library, you can mark functions that should visible in created library with ``@exported(name: String)`` annotation. In case if you omit or use null as the argument for name 
extern function name match the name of the method.
Currently, only static object methods can be exported. To export accessors of field or variable in static object use ``@exportAccessors(getterName: String, setterName: String)``. 
If you omit the explicit names in the annotation constructor, Scala Native would create exported methods with ``set_`` and ``get_`` prefixes and name of field.

`int ScalaNativeInit(void);` function is special exported function that needs to be called before invoking any code defined in Scala Native.
It returns `0` on successful initialization and non-zero value in the otherwise.
For dynamic libraries a constructor would be generated to invoke `ScalaNativeInit`` function automatically upon loading library or startup of the program.
If for some reason you need to disable automatic initialization of Scala Native upon loading dynamic library and invoke it manually in user code set `SCALANATIVE_NO_DYLIB_CTOR` environment variable.
You can also disable generation of library constructors by defining `-DSCALANATIVE_NO_DYLIB_CTOR` in NativeConfig::compileOptions of your build.

.. code-block:: scala

    import scala.scalanative.unsafe._

    object myLib{
      @exportAccessors("mylib_current_count", "mylib_set_counter")
      var counter: Int = 0

      @exportAccessors("error_message")
      val ErrorMessage: CString = c"Something bad just happend!"

      @exported
      def addLongs(l: Long, r: Long): Long = l + r

      @exported("mylib_addInts")
      def addInts(l: Int, r: Int): Int = l + r
    }

.. code-block:: c

    // libmylib.h
    int ScalaNativeInit(void);
    long addLongs(long, long);
    int mylib_addInts(int, int);
    int mylib_current_count();
    void mylib_set_counter(int);

    // test.c
    #include "libmylib.h"
    #include <assert.h>
    #include <stdio.h>

    int main(int argc, char** argv){
      // This function needs to be called before invoking any methods defined in Scala Native.
      // Might be called automatically unless SCALANATIVE_NO_DYLIB_CTOR env variable is set.
      assert(ScalaNativeInit() == 0);
      addLongs(0L, 4L);
      mylib_addInts(4, 0);
      printf("Current count %d\n", mylib_current_count());
      mylib_set_counter(42);
      // ...
    }

Pointer types
-------------

Scala Native provides a built-in equivalent of C's pointers via
``unsafe.Ptr[T]`` data type. Under the hood pointers are implemented
using unmanaged machine pointers.

Operations on pointers are closely related to their C counterparts and
are compiled into equivalent machine code:

================ ======================== ===================
Operation        C syntax                 Scala Syntax
================ ======================== ===================
Load value       ``*ptr``                 ``!ptr``
Store value      ``*ptr = value``         ``!ptr = value``
Pointer to index ``ptr + i``, ``&ptr[i]`` ``ptr + i``
Elements between ``ptr1 - ptr2``          ``ptr1 - ptr2``
Load at index    ``ptr[i]``               ``ptr(i)``
Store at index   ``ptr[i] = value``       ``ptr(i) = value``
Pointer to field ``&ptr->name``           ``ptr.atN``
Load a field     ``ptr->name``            ``ptr._N``
Store a field    ``ptr->name = value``    ``ptr._N = value``
================ ======================== ===================

Where ``N`` is the index of the field ``name`` in the struct.
See `Memory layout types`_ for details.

Function pointers
`````````````````

It is possible to use external functions that take function pointers. For
example given the following signature in C:

.. code-block:: C

    void test(void (* f)(char *));

One can declare it as follows in Scala Native:

.. code-block:: scala

    def test(f: unsafe.CFuncPtr1[CString, Unit]): Unit = unsafe.extern

`CFuncPtrN` types are final classes containing pointer to underlying
C function pointer. They automatically handle boxing call arguments
and unboxing result. You can create them from C pointer using `CFuncPtr` helper methods:

.. code-block:: scala

    def fnDef(str: CString): CInt = ???

    val anyPtr: CVoidPtr = CFuncPtr.toPtr {
      CFuncPtr1.fromScalaFunction(fnDef)
    }

    type StringLengthFn = CFuncPtr1[CString, CInt]
    val func: StringLengthFn = CFuncPtr.fromPtr[StringLengthFn](anyPtr)
    func(c"hello")

It's also possible to create `CFuncPtrN` from Scala `FunctionN`.
You can do this by using implicit method conversion method
from the corresponding companion object.

.. code-block:: scala

   import scalanative.unsafe.CFuncPtr0
   def myFunc(): Unit = println("hi there!")

   val myFuncPtr: CFuncPtr0[Unit] = CFuncPtr0.fromScalaFunction(myFunc)
   val myImplFn: CFuncPtr0[Unit] = myFunc _
   val myLambdaFuncPtr: CFuncPtr0[Unit] = () => println("hello!")

On Scala 2.12 or newer, the Scala language automatically converts
from closures to SAM types:

.. code-block:: scala

   val myfuncptr: unsafe.CFuncPtr0[Unit] = () => println("hi there!")

Memory management
`````````````````

Unlike standard Scala objects that are managed automatically by the underlying
runtime system, one has to be extra careful when working with unmanaged memory.

1. **Zone allocation.** (since 0.3)

   Zones (also known as memory regions/contexts) are a technique for
   semi-automatic memory management. Using them one can bind allocations
   to a temporary scope in the program and the zone allocator will
   automatically clean them up for you as soon as execution goes out of it:

   .. code-block:: scala

      import scala.scalanative.unsafe._

      Zone { implicit z =>
        val buffer = alloc[Byte](n)
      }

   ``alloc`` requests memory sufficient to contain `n` values of a given type.
   If number of elements is not specified, it defaults to a single element.
   Memory is zeroed out by default.

   Zone allocation is the preferred way to allocate temporary unmanaged memory.
   It's idiomatic to use implicit zone parameters to abstract over code that
   has to zone allocate.

   One typical example of this are C strings that are created from
   Scala strings using ``unsafe.toCString``. The conversion takes implicit
   zone parameter and allocates the result in that zone.

   When using zone allocated memory one has to be careful not to
   capture this memory beyond the lifetime of the zone. Dereferencing
   zone-allocated memory after the end of the zone is undefined behavior.

2. **Stack allocation.**

   Scala Native provides a built-in way to perform stack allocations of
   using ``unsafe.stackalloc`` function:

   .. code-block:: scala

       val buffer = unsafe.stackalloc[Byte](256)

   This code will allocate 256 bytes that are going to be available until
   the enclosing method returns. Number of elements to be allocated is optional
   and defaults to 1 otherwise. Memory **is zeroed out** by default.

   When using stack allocated memory one has to be careful not to capture
   this memory beyond the lifetime of the method. Dereferencing stack allocated
   memory after the method's execution has completed is undefined behavior.

3. **Manual heap allocation.**

   Scala Native's library contains a bindings for a subset of the standard
   libc functionality. This includes the trio of ``malloc``, ``realloc`` and
   ``free`` functions that are defined in ``unsafe.stdlib`` extern object.

   Calling those will let you allocate memory using system's standard
   dynamic memory allocator. Every single manual allocation must also
   be freed manually as soon as it's not needed any longer.

   Apart from the standard system allocator one might
   also bind to plethora of 3-rd party allocators such as jemalloc_ to
   serve the same purpose.

.. Comment - https does not work with jemalloc.net
.. _jemalloc: http://jemalloc.net/

Undefined behavior
``````````````````

Similarly to their C counter-parts, behavior of operations that
access memory is subject to undefined behaviour for following conditions:

1. Dereferencing null.
2. Out-of-bounds memory access.
3. Use-after-free.
4. Use-after-return.
5. Double-free, invalid free.

Memory layout types
```````````````````

Memory layout types are auxiliary types that let one specify memory layout of
unmanaged memory. They are meant to be used purely in combination with native
pointers and do not have a corresponding first-class values backing them.

* ``unsafe.Ptr[unsafe.CStructN[T1, ..., TN]]``

  Pointer to a C struct with up to 22 fields.
  Type parameters are the types of corresponding fields.
  One may access fields of the struct using ``_N`` helper
  methods on a pointer value:

  .. code-block:: scala

      val ptr = unsafe.stackalloc[unsafe.CStruct2[Int, Int]]()
      ptr._1 = 10
      ptr._2 = 20
      println(s"first ${ptr._1}, second ${ptr._2}")

  Here ``_N`` is an accessor for the field number N.

* ``unsafe.Ptr[unsafe.CArray[T, N]]``

  .. Wizardry and lore ahead!
  ..
  .. Sphinx & Pygments warn that they can not parse & highlight next code-block
  .. as Scala. Use double colon code-block idiom to avoid build warning.
  .. Default Python style will highlight code-block "close enough" to Scala.

  Pointer to a C array with statically-known length ``N``. Length is encoded as
  a type-level natural number. Natural numbers are types that are composed of
  base naturals ``Nat._0, ... Nat._9`` and an additional ``Nat.DigitN``
  constructors, where ``N`` refers to number of digits in the given number. 
  So for example number ``1024`` is going to be encoded as following::

      import scalanative.unsafe._, Nat._

      type _1024 = Digit4[_1, _0, _2, _4]

  .. Sphinx & Pygments warn that they can not parse & highlight next code-block
  .. as Scala. Use double colon code-block idiom to avoid build warning.
  .. There will be a slight visual glitch because default Python will not.
  .. highlight it.

  Once you have a natural for the length, it can be used as an array length::

      val arrptr = unsafe.stackalloc[CArray[Byte, _1024]]()

  You can find an address of n-th array element via ``arrptr.at(n)``.

Byte strings
````````````

Scala Native supports byte strings via ``c"..."`` string interpolator
that gets compiled down to pointers to statically-allocated zero-terminated
strings (similarly to C):

.. code-block:: scala

    import scalanative.unsafe._
    import scalanative.libc._

    // CString is an alias for Ptr[CChar]
    val msg: CString = c"Hello, world!"
    stdio.printf(msg)

It does not allow any octal values or escape characters not supported by Scala compiler, like ``\a`` or ``\?``, but also unicode escapes.
It is possible to use C-style hex values up to value 0xFF, eg. ``c"Hello \x61\x62\x63"``

Additionally, we also expose two helper functions ``unsafe.fromCString`` and ``unsafe.toCString``
to convert between C-style `CString` (sequence of Bytes, usually interpreted as UTF-8 or ASCII)
and Java-style `String` (sequence of 2-byte Chars usually interpreted as UTF-16).

It's worth to remember that ``unsafe.toCString`` and `c"..."` interpreter cannot be used interchangeably as they handle literals differently.
Helper methods ``unsafe.fromCString` and ``unsafe.toCString`` are charset aware.
They will always assume `String` is UTF-16, and take a `Charset` parameter to know what encoding to assume for the byte string (`CString`) - if not present it is UTF-8.

If passed a null as an argument, they will return a null of the appropriate
type instead of throwing a NullPointerException.


Platform-specific types
-----------------------

Scala Native defines the type ``Size`` and its unsigned counterpart, ``USize``.
A size corresponds to ``Int`` on 32-bit architectures and to ``Long`` on 64-bit
ones.

Size and alignment of types
---------------------------

In order to statically determine the size of a type, you can use the ``sizeof``
function which is Scala Native's counterpart of the eponymous C operator. It
returns the size in bytes:

.. code-block:: scala

    println(unsafe.sizeof[Byte])    // 1
    println(unsafe.sizeof[CBool])   // 1
    println(unsafe.sizeof[CShort])  // 2
    println(unsafe.sizeof[CInt])    // 4
    println(unsafe.sizeof[CLong])   // 8

It can also be used to obtain the size of a structure:

.. code-block:: scala

    type TwoBytes = unsafe.CStruct2[Byte, Byte]
    println(unsafe.sizeof[TwoBytes])  // 2

Additionally, you can also use ``alignmentof`` to find the alignment of a given type:

.. code-block:: scala

    println(unsafe.alignmentof[Int])                         // 4
    println(unsafe.alignmentof[unsafe.CStruct2[Byte, Long]]) // 8

Unsigned integer types
----------------------

Scala Native provides support for four unsigned integer types:

1. ``unsigned.UByte``
2. ``unsigned.UShort``
3. ``unsigned.UInt``
4. ``unsigned.ULong``
5. ``unsigned.USize``

They share the same primitive operations as signed integer types.
Primitive operation between two integer values are supported only
if they have the same signedness (they must both signed or both unsigned.)

Conversions between signed and unsigned integers must be done explicitly
using ``byteValue.toUByte``, ``shortValue.toUShort``, ``intValue.toUInt``, ``longValue.toULong``, ``sizeValue.toUSize``
and conversely ``unsignedByteValue.toByte``, ``unsignedShortValue.toShort``, ``unsignedIntValue.toInt``,
``unsignedLongValue.toLong``, ``unsignedSizeValue.toSize``.

Universal equality is supported between signed and unsigned integers, for example ``-1.toUByte == 255`` or ``65535 == -1.toUShort`` would yield ``true``, 
However, similar to signed integers on JVM, class equality between different (boxed) integer types is not supported.
Usage of `-1.toUByte.equals(255)` would return ``false``, as we're comparing different boxed types (``scala.scalanative.unsigned.UByte`` with ``java.lang.Integer``)

Continue to :ref:`native`.
