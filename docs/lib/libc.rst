.. _libc:

C Standard Library
==================

Scala Native provides bindings for the core subset of the
`C standard library <http://en.cppreference.com/w/c/header>`_:

============== ==================================
C Header       Scala Native Module
============== ==================================
assert.h_      N/A
complex.h_     N/A
ctype.h_       scala.scalanative.native.ctype_
errno.h_       scala.scalanative.native.errno_
fenv.h_        N/A
float.h_       N/A
inttypes.h_    N/A
iso646.h_      N/A
limits.h_      N/A
locale.h_      N/A
math.h_        scala.scalanative.native.math_
setjmp.h_      N/A
signal.h_      N/A
stdalign.h_    N/A
stdarg.h_      N/A
stdatomic.h_   N/A
stdbool.h_     N/A
stddef.h_      N/A
stdint.h_      N/A
stdio.h_       scala.scalanative.native.stdio_
stdlib.h_      scala.scalanative.native.stdlib_
stdnoreturn.h_ N/A
string.h_      scala.scalanative.native.string_
tgmath.h_      N/A
threads.h_     N/A
time.h_        N/A
uchar.h_       N/A
wchar.h_       N/A
wctype.h_      N/A
============== ==================================

.. _assert.h: http://en.cppreference.com/w/c/error
.. _complex.h: http://en.cppreference.com/w/c/numeric/complex
.. _ctype.h: http://en.cppreference.com/w/c/string/byte
.. _errno.h: http://en.cppreference.com/w/c/error
.. _fenv.h: http://en.cppreference.com/w/c/numeric/fenv
.. _float.h: http://en.cppreference.com/w/c/types/limits#Limits_of_floating_point_types
.. _inttypes.h: http://en.cppreference.com/w/c/types/integer
.. _iso646.h: http://en.cppreference.com/w/c/language/operator_alternative
.. _limits.h: http://en.cppreference.com/w/c/types/limits
.. _locale.h: http://en.cppreference.com/w/c/locale
.. _math.h: http://en.cppreference.com/w/c/numeric/math
.. _setjmp.h: http://en.cppreference.com/w/c/program
.. _signal.h: http://en.cppreference.com/w/c/program
.. _stdalign.h: http://en.cppreference.com/w/c/types
.. _stdarg.h: http://en.cppreference.com/w/c/variadic
.. _stdatomic.h: http://en.cppreference.com/w/c/atomic
.. _stdbool.h: http://en.cppreference.com/w/c/types/boolean
.. _stddef.h: http://en.cppreference.com/w/c/types
.. _stdint.h: http://en.cppreference.com/w/c/types/integer
.. _stdio.h: http://en.cppreference.com/w/c/io
.. _stdlib.h:
.. _stdnoreturn.h: http://en.cppreference.com/w/c/types
.. _string.h: http://en.cppreference.com/w/c/string/byte
.. _tgmath.h: http://en.cppreference.com/w/c/numeric/tgmath
.. _threads.h: http://en.cppreference.com/w/c/thread
.. _time.h: http://en.cppreference.com/w/c/chrono
.. _uchar.h: http://en.cppreference.com/w/c/string/multibyte
.. _wchar.h: http://en.cppreference.com/w/c/string/wide
.. _wctype.h: http://en.cppreference.com/w/c/string/wide

.. _scala.scalanative.native.ctype: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/ctype.scala
.. _scala.scalanative.native.errno: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/errno.scala
.. _scala.scalanative.native.math: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/math.scala
.. _scala.scalanative.native.stdio: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/stdio.scala
.. _scala.scalanative.native.stdlib: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/stdlib.scala
.. _scala.scalanative.native.string: https://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/string.scala

Continue to :ref:`faq`.

