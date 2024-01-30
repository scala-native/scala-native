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

Scala Native supports parallel multi-threaded programming and assumes multi-threaded execution by default.
Upon the absence of system threads in the linked program, Scala Native can automatically switch to single-threaded mode, 
allowing to get rid of redundant synchronization, as the state is never shared between threads.

Scala Native tries to follow the Java Memory Model, but by default uses more relaxed semantics in some areas. 
Due to the majority of immutable shared states in most Scala programs, Scala Native does not follow Java final fields semantics. 
Safe publication of final fields (`val`s in Scala) can be enabled by annotating fields or the whole class with `@scala.scalanative.annotation.safePublish`.

Scala Native ensures that all class field operations would be executed atomically, but does not impose any synchronization or happens-before guarantee. 


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
