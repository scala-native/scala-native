.. _native:

Native Code in your Application or Library
==========================================

Scala Native uses native C and C++ code to interact with the underlying
platform and operating system. Since the tool chain compiles and links
the Scala Native system, it can also compile and link C and C++ code
included in an application project or a library that supports Scala
Native that includes C and/or C++ source code.

Supported file extensions for native code are `.c`, `.cpp`, and `.S`.

Note that `.S` files or assembly code is not portable across different CPU
architectures so conditional compilation would be needed to support
more than one architecture. You can also include header files with
the extensions `.h` and `.hpp`.

Applications with Native Code
-----------------------------

In order to create standalone native projects with native code use the
following procedure. You can start with the basic Scala Native template.

Add C/C++ code into `src/main/resources/scala-native`. The code can be put in
subdirectories as desired inside the `scala-native` directory. As an example,
create a file named `myapi.c` and put it into your `scala-native` directory
as described above.

.. code-block:: c

    long long add3(long long in) { return in + 3; }

Next, create a main file as follows:

.. code-block:: scala

    import scalanative.unsafe._

    @extern
    object myapi {
      def add3(in: CLongLong): CLongLong = extern
    }

    object Main {
      import myapi._
      def main(args: Array[String]): Unit = {
        val res = add3(-3L)
        assert(res == 0L)
        println(s"Add3 to -3 = $res")
      }
    }

Finally, compile and run this like a normal Scala Native application.

Using libraries with Native Code
------------------------------------------

Libraries developed to target the Scala Native platform
can have C, C++, or assembly files included in the dependency. The code is
added to `src/main/resources/scala-native` and is published like a normal
Scala library. The code can be put in subdirectories as desired inside the
`scala-native` directory. These libraries can also be cross built to
support Scala/JVM or Scala.js if the Native portions have replacement
code on the respective platforms.

The primary purpose of this feature is to allow libraries to support
Scala Native that need native "glue" code to operate. The current
C interopt does not allow direct access to macro defined constants and
functions or allow passing "struct"s from the stack to C functions.
Future versions of Scala Native may relax these restrictions making
this feature obsolete.

Note: This feature is not a replacement for developing or distributing
native C/C++ libraries and should not be used for this purpose.

If the dependency contains native code, Scala Native will identify the
library as a dependency that has native code and will unpack the library.
Next, it will compile, link, and optimize any native code along with the
Scala Native runtime and your application code. No additional information
is needed in the build file other than the normal dependency so it is
transparent to the library user.

Continue to :ref:`testing`.
