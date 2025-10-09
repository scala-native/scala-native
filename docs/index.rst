
Scala Native
============


+----------------------+------------------------+
| Releases [1]_                                 |
+======================+========================+
| Type                 | Version                |
+----------------------+------------------------+
| Stable               | |last_stable_release|  |
+----------------------+------------------------+
| Latest               | |release|              |
+----------------------+------------------------+

Scala Native is an optimizing ahead-of-time compiler and lightweight managed
runtime designed specifically for Scala. It features:

* **Low-level primitives**.

  .. code-block:: scala

      import scala.scalanative.unsafe._

      type Vec = CStruct3[Double, Double, Double]

      def sum(vec: Ptr[Vec]): Double =
        return vec._1 + vec._2 + vec._3

      @main def main(): Unit =
        val vec = stackalloc[Vec]() // allocate c struct on stack
        vec._1 = 10.0             // initialize fields
        vec._2 = 20.0
        vec._3 = 30.0
        println(sum(vec))         // pass by reference

  Pointers, structs, you name it. Low-level primitives
  let you hand-tune your application to make it work
  exactly as you want it to. You're in control.

* **Seamless interop with native code**.

  .. code-block:: scala

      import scala.scalanative.unsafe._
      import scala.scalanative.unsigned._

      @extern object stdlib {
        def malloc(size: CSize): Ptr[Byte] = extern
      }

      val ptr = stdlib.malloc(32.toCSize)

  Calling C code has never been easier.
  With the help of extern objects you can
  seamlessly call native code without any
  runtime overhead.

* **Instant startup time**.

  .. code-block:: text

      > time hello-native
      hello, native!

      real    0m0.005s
      user    0m0.002s
      sys     0m0.002s

  Scala Native is compiled ahead-of-time via LLVM.
  This means that there is no sluggish warm-up
  phase that's common for just-in-time compilers.
  Your code is immediately fast and ready for action.

Community
---------

* Want to follow project updates?
  `Follow us on twitter <https://twitter.com/scala_native>`_.

* Want to chat?
  Join `our Discord channel <https://discord.com/invite/scala>`_.

* Have a question?
  Ask it on `Stack Overflow with tag scala-native <https://stackoverflow.com/questions/tagged/scala-native>`_.

* Found a bug or want to propose a new feature?
  Open `an issue on GitHub <https://github.com/scala-native/scala-native/issues>`_.

Documentation
-------------

This documentation is divided into
different parts. It's recommended to go through the :ref:`user` to get familiar
with Scala Native. :ref:`lib` will walk you through all the known libraries that
are currently available. :ref:`contrib` contains valuable information for people
who want to either contribute to the project or learn more about the internals
and the development process behind the project.

.. toctree::
  :maxdepth: 2

  user/index
  lib/index
  contrib/index
  blog/index
  changelog/index
  faq

.. [1] See :ref:`release-types`

Document built at : |today|
