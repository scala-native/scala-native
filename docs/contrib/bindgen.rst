Automatic binding generation
============================

Binding generation for now is experimental. The goal is to first use it
internally to generate bindings for C's stdlib and later make it
available via scala.meta macros and sbt tasks.

The bindgen library uses libclang which is a C API for inspecting
clang's AST. libclang does not provide all the details available in the
C++ API, but is considered more stable and as such is preferable when
supporting multiple versions of clang.

The majority of the bindgen library is written in Scala. However, due to
Scala Native's missing support for passing structs by value a small
libclang wrapper written in C exposes the API using only pointer types.
The wrapper makes heavy use of C macros to reduce boilerplate and allows
to generate the Scala bindings. As bindgen matures part of the libclang
bindings can be generated using the bindgen library itself.

Run the following command to update the Scala bindings from the C code::

    $ clang -DSCALA -E nativelib/src/main/resources/clang.c \
        | grep -v '^#' \
        > nativelib/src/main/scala/scala/scalanative/runtime/bindgen/Clang.scala

Tips
----

Clang's AST can be inspected in multiple ways using the CLI tools::

    $ clang -Xclang -ast-dump /usr/include/stdio.h | less -R
    $ clang-check -ast-dump -ast-dump-filter=vsprintf /usr/include/stdio.h

This gives a good idea how to match a specific part of the AST using the API.

Resources
---------

 - `The libclang API <http://clang.llvm.org/doxygen/group__CINDEX.html>`_
 - `The Clang AST <https://www.youtube.com/embed/VqCkCDFLSsc?vq=hd720>`_
 - `Intro to libclang <http://bastian.rieck.ru/blog/posts/2015/baby_steps_libclang_ast/>`_
