.. _interop:

Native code interoperability
============================

Scala Native provides an interop layer that makes it easy to interact with
foreign native code. This includes C and other languages that can expose APIs
via C ABI (e.g. C++, D, Rust etc.)

Extern objects
--------------

Extern objects are simple wrapper objects that demarcate scopes where methods
and fields are treated as their native C ABI-friendly counterparts. They are
roughly analagous to header files in C.

For example to call C's ``malloc`` one might declare it as following:

.. code-block:: scala

    @extern object libc {
      def malloc(size: CSize): Ptr[Byte] = extern
    }

``extern`` on the right hand side of the method definition signifies
that the body of the method is defined elsewhere in a native library that is
available on the library path (see `Linking with native libraries`_.) Signature
of the extern function must match the signature of the original C function
(see `Finding the right signature`_.)

Apart from calling extern methods, one may also observe global state
defined in native libraries. To access global variable ``myvariable``
defined in C:

.. code-block:: c

    int myvariable;

One can declare it as following in Scala:

.. code-block:: scala

    @extern object mylib {
      var myvariable: CInt = extern
    }

Reads and write to `myvariable` will be mapped to reads and writes to a
corresponding external global variable.

Finding the right signature
```````````````````````````

To find a correct signature for a given C function one must provide an
equivalent Scala type for each of the arguments (wher all of the types with
``C`` prefix and ``Ptr``, are defined in ``scala.scalanative.native`` package):

===================== =========================
C Type                Scala Type
===================== =========================
void                  Unit
bool                  CBool
char, signed char     CChar
unsigned char         CUnsignedChar (1)
short                 CShort
unsigned short        CUnsignedShort (1)
int                   CInt
unsigned int          CUnsignedInt (1)
long                  CLong
unsigned long         CUnsignedLong (1)
long long             CLongLong
unsigned long long    CUnsignedLongLong (1)
size_t                CSize
wchar_t               CWideChar
char16_t              CChar16
char32_t              CChar32
float                 CFloat
double                CDouble
void*                 Ptr[Byte] (2)
int*                  Ptr[CInt] (2)
char*                 CString (2) (3)
int (\*)(int)         CFunctionPtr1[CInt, CInt] (2) (4)
struct { int x, y; }* Ptr[CStruct2[CInt, Cint]] (2) (5)
struct { int x, y; }  N/A (6)
===================== =========================

(1) See `Unsigned integer types`_.
(2) See `Pointer types`_.
(3) See `Byte strings`_.
(4) See `Function pointers`_.
(5) See `Memory layout types`_.
(6) See `Passing structs by value`_.

Linking with native libraries
`````````````````````````````

In C/C++ one has to typically pass an additional ``-l mylib`` flag to link with
a library. In Scala Native one can annotate libraries to link with using
``@link`` annotation:

.. code-block:: scala

   @link("mylib")
   @extern object mylib {
     ...
   }

Whenever any of the members of ``mylib`` object are reachable, the Scala Native
linker will automatically link with the corresponding native library.

Variadic functions
``````````````````

One can declare variadic functions like ``printf`` using ``CVararg`` auxiliary
type:

.. code-block:: scala

   @extern object stdio {
     def printf(format: CString, args: CVararg*): CInt = extern
   }

Passing structs by value
````````````````````````

At the moment we do not support passing C structs by value to extern functions.

Pointer types
-------------

Stack allocation
````````````````

Heap allocation
```````````````

Memory layout types
```````````````````

Memory layout types are auxiliary types that let one specify memory layout of
unmanaged memory. They are meant to be used purely in combination with pointers
and do not have a corresponding first-class values backing them.

* ``Ptr[CStructN[T1, ..., TN]]``

  Pointer to a C struct with up to 22 fields.
  Type parameters are the types of corresponding fields.
  One may access fields of the struct using ``_N`` helper
  methods on a pointer value:

  .. code-block:: scala

      val ptr = stackalloc[CStruct[Int, Int]]
      !ptr._1 = 10
      !ptr._2 = 20
      println(s"first ${!ptr_.1}, second ${!ptr._2}")

  Here ``_N`` computes a derived pointer that corresponds to memory
  occupied by field number N.

* ``Ptr[CArray[T, N]]``

  Pointer to a C array with statically-known length ``N``. Length is encoded as
  a type-level natural number. Natural numbers are types that are composed of
  base naturals ``Nat._0, ... Nat._9`` and an additional ``Nat.Digit``
  constructor. So for example number ``1024`` is going to be encoded as
  following:

  .. code-block:: scala

      import scalanative.Nat._

      type _1024 = Digit[_1, Digit[_0, Digit[_2, _4]]]

  Once you have a natural for the length, it can be used as an array length:

  .. code-block:: scala

      val ptr = stackalloc[CArray[Byte, _1024]]

  Addresses of the first twenty two elements are accessible via ``_N``
  accessors. The rest are accessible via ``ptr._1 + index``.

Byte strings
````````````

Function pointers
`````````````````

Unsafe casts
````````````

Unsigned integer types
----------------------

Continue to :ref:`lib`.
