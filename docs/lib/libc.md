# ISO/IEC C Standard Library

Scala Native provides bindings for a core subset of the International
Organization for Standardization/International Electrotechnical
Commission (ISO/IEC) [C standard
library](https://en.cppreference.com/w/c/header).

The project now tracks the *C11 standard (ISO/IEC 9899:2011)* but
currently most bindings are from the *C99 standard (ISO/IEC 9899:1999)*.

  C Header                                                                                 Scala Native Module
  ---------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------------------------------------------------------
  [assert.h](https://en.cppreference.com/w/c/error)                                        N/A - *indicates binding not available*
  [complex.h](https://en.cppreference.com/w/c/numeric/complex)                             [scala.scalanative.libc.complex](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/complex.scala)
  [ctype.h](https://en.cppreference.com/w/c/string/byte)                                   [scala.scalanative.libc.ctype](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/ctype.scala)
  [errno.h](https://en.cppreference.com/w/c/error)                                         [scala.scalanative.libc.errno](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/errno.scala)
  [fenv.h](https://en.cppreference.com/w/c/numeric/fenv)                                   N/A
  [float.h](https://en.cppreference.com/w/c/types/limits#Limits_of_floating_point_types)   [scala.scalanative.libc.float](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/float.scala)
  [inttypes.h](https://en.cppreference.com/w/c/types/integer)                              N/A
  [iso646.h](https://en.cppreference.com/w/c/language/operator_alternative)                N/A
  [limits.h](https://en.cppreference.com/w/c/types/limits)                                 N/A
  [locale.h](https://en.cppreference.com/w/c/locale)                                       [scala.scalanative.libc.locale](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/locale.scala)
  [math.h](https://en.cppreference.com/w/c/numeric/math)                                   [scala.scalanative.libc.math](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/math.scala)
  [setjmp.h](https://en.cppreference.com/w/c/program)                                      N/A
  [signal.h](https://en.cppreference.com/w/c/program)                                      [scala.scalanative.libc.signal](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/signal.scala)
  [stdalign.h](https://en.cppreference.com/w/c/types)                                      N/A
  [stdarg.h](https://en.cppreference.com/w/c/variadic)                                     N/A
  [stdatomic.h](https://en.cppreference.com/w/c/atomic)                                    N/A
  [stdbool.h](https://en.cppreference.com/w/c/types/boolean)                               N/A
  [stddef.h](https://en.cppreference.com/w/c/types)                                        [scala.scalanative.libc.stddef](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/stddef.scala)
  [stdint.h](https://en.cppreference.com/w/c/types/integer)                                N/A
  [stdio.h](https://en.cppreference.com/w/c/io)                                            [scala.scalanative.libc.stdio](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/stdio.scala)
  [stdlib.h](https://en.cppreference.com/w/cpp/header/cstdlib)                             [scala.scalanative.libc.stdlib](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/stdlib.scala)
  [stdnoreturn.h](https://en.cppreference.com/w/c/types)                                   N/A
  [string.h](https://en.cppreference.com/w/c/string/byte)                                  [scala.scalanative.libc.string](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/string.scala)
  [tgmath.h](https://en.cppreference.com/w/c/numeric/tgmath)                               N/A
  [threads.h](https://en.cppreference.com/w/c/thread)                                      N/A
  [time.h](https://en.cppreference.com/w/c/chrono)                                         [scala.scalanative.libc.time](https://github.com/scala-native/scala-native/blob/main/clib/src/main/scala/scala/scalanative/libc/time.scala)
  [uchar.h](https://en.cppreference.com/w/c/string/multibyte)                              N/A
  [wchar.h](https://en.cppreference.com/w/c/string/wide)                                   N/A
  [wctype.h](https://en.cppreference.com/w/c/string/wide)                                  N/A

Continue to `posixlib`{.interpreted-text role="ref"}.
