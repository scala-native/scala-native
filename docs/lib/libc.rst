.. _libc:

ISO/IEC C Standard Library
==========================

Scala Native provides bindings for a core subset of the
International Organization for Standardization/International
Electrotechnical Commission (ISO/IEC)
`C standard library <https://en.cppreference.com/w/c/header>`_:

============== ==================================
C Header       Scala Native Module
============== ==================================
assert.h_      N/A - *indicates binding not available*
complex.h_     scala.scalanative.libc.complex_
ctype.h_       scala.scalanative.libc.ctype_
errno.h_       scala.scalanative.libc.errno_
fenv.h_        N/A
float.h_       scala.scalanative.libc.float_
inttypes.h_    N/A
iso646.h_      N/A
limits.h_      N/A
locale.h_      scala.scalanative.libc.locale_
math.h_        scala.scalanative.libc.math_
setjmp.h_      N/A
signal.h_      scala.scalanative.libc.signal_
stdalign.h_    N/A
stdarg.h_      N/A
stdatomic.h_   N/A
stdbool.h_     N/A
stddef.h_      scala.scalanative.libc.stddef_
stdint.h_      N/A
stdio.h_       scala.scalanative.libc.stdio_
stdlib.h_      scala.scalanative.libc.stdlib_
stdnoreturn.h_ N/A
string.h_      scala.scalanative.libc.string_
tgmath.h_      N/A
threads.h_     N/A
time.h_        N/A
uchar.h_       N/A
wchar.h_       N/A
wctype.h_      N/A
============== ==================================

.. _assert.h: https://en.cppreference.com/w/c/error
.. _complex.h: https://en.cppreference.com/w/c/numeric/complex
.. _ctype.h: https://en.cppreference.com/w/c/string/byte
.. _errno.h: https://en.cppreference.com/w/c/error
.. _fenv.h: https://en.cppreference.com/w/c/numeric/fenv
.. _float.h: https://en.cppreference.com/w/c/types/limits#Limits_of_floating_point_types
.. _inttypes.h: https://en.cppreference.com/w/c/types/integer
.. _iso646.h: https://en.cppreference.com/w/c/language/operator_alternative
.. _limits.h: https://en.cppreference.com/w/c/types/limits
.. _locale.h: https://en.cppreference.com/w/c/locale
.. _math.h: https://en.cppreference.com/w/c/numeric/math
.. _setjmp.h: https://en.cppreference.com/w/c/program
.. _signal.h: https://en.cppreference.com/w/c/program
.. _stdalign.h: https://en.cppreference.com/w/c/types
.. _stdarg.h: https://en.cppreference.com/w/c/variadic
.. _stdatomic.h: https://en.cppreference.com/w/c/atomic
.. _stdbool.h: https://en.cppreference.com/w/c/types/boolean
.. _stddef.h: https://en.cppreference.com/w/c/types
.. _stdint.h: https://en.cppreference.com/w/c/types/integer
.. _stddef.h: https://en.cppreference.com/w/c/types
.. _stdio.h: https://en.cppreference.com/w/c/io
.. _stdlib.h: https://en.cppreference.com/w/cpp/header/cstdlib
.. _stdnoreturn.h: https://en.cppreference.com/w/c/types
.. _string.h: https://en.cppreference.com/w/c/string/byte
.. _tgmath.h: https://en.cppreference.com/w/c/numeric/tgmath
.. _threads.h: https://en.cppreference.com/w/c/thread
.. _time.h: https://en.cppreference.com/w/c/chrono
.. _uchar.h: https://en.cppreference.com/w/c/string/multibyte
.. _wchar.h: https://en.cppreference.com/w/c/string/wide
.. _wctype.h: https://en.cppreference.com/w/c/string/wide

.. _scala.scalanative.libc.complex: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/complex.scala
.. _scala.scalanative.libc.ctype: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/ctype.scala
.. _scala.scalanative.libc.errno: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/errno.scala
.. _scala.scalanative.libc.float: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/float.scala
.. _scala.scalanative.libc.locale: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/locale.scala
.. _scala.scalanative.libc.math: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/math.scala
.. _scala.scalanative.libc.stddef: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/stddef.scala
.. _scala.scalanative.libc.stdio: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/stdio.scala
.. _scala.scalanative.libc.stdlib: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/stdlib.scala
.. _scala.scalanative.libc.string: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/string.scala
.. _scala.scalanative.libc.signal: https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/signal.scala

Continue to :ref:`posixlib`.

