.. _lang:

Language semantics
==================

In general, the semantics of the Scala Native language are the same as Scala on
the JVM. However, a few differences exist, which we mention here.

Interop extensions
------------------

Annotations and types defined ``scala.scalanative.unsafe`` may modify semantics
of the language for sake of interoperability with C libraries, read more about
those in :ref:`interop` section.

Multithreading
--------------

Scala Native doesn't yet provide libraries for parallel multi-threaded
programming and assumes single-threaded execution by default.

It's possible to use C libraries to get access to multi-threading and
synchronization primitives but this is not officially supported at the moment.

Finalization
------------

Finalize method from ``java.lang.Object`` is never called in Scala Native.

Undefined behavior
------------------

Generally, Scala Native follows most of the special error conditions
similarly to JVM:

1. Arrays throw ``IndexOutOfBoundsException`` on out-of-bounds access.
2. Casts throw ``ClassCastException`` on incorrect casts.
3. Accessing a field or method on ``null``, throwing ``null``` exception, throws ``NullPointerException``.
4. Integer division by zero throws ``ArithmeticException``.

There are a few exceptions:

1. Stack overflows are undefined behavior and would typically segfault on supported architectures instead of throwing ``StackOverflowError``.
2. Exhausting a heap space results in crash with a stack trace instead of throwing ``OutOfMemoryError``.

Continue to :ref:`interop`.
