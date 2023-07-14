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

Using a library that contains native code can be used in combination with
the feature above that allows native code in your application.

EXPERIMENTAL: Deployment Descriptor for Conditional Compilation
---------------------------------------------------------------

This is an **experimental** feature. Scala Native may deprecate or
remove this feature with little or no warning.

Libraries developed with "glue" code as described in the above section
can cause compilation errors when all the following conditions occur:

1. The library and/or header files are not installed

2. The dependency is in the library users' build

3. The code that uses the "glue" code is not called by the application
   or library

If the glue "code" is being called, then the library and headers need to
be installed to compile your application otherwise errors are expected.

Scala Native code can include the annotation ``@link("z")`` for example
that says link with the ``z`` library. The compiler will add a link
option for this library to the linking phase of the build if the code
with the annotation is used. See :ref:`interop`,
`Linking with native libraries` section for more information.

This **experimental** feature has been added so the users of your published
library can avoid the error described above. Use the following procedure to
implement this feature.

1. Add a Scala Native deployment descriptor to your library. For the
purposes of this example assume the library is ``z``. The properties file
must be named ``scala-native.properties`` and must be put in the base of the
``src/main/resources/scala-native`` directory.

2. Add the following content to your new ``scala-native.properties`` file.
Note that if your library has more that one library you can add a comma
delimited list of libraries. If desired, the comments are not needed.

.. code-block:: properties

    # configuration for glue code
    # defines SCALANATIVE_LINK_Z if @link("z") annnotation is used (found in NIR)
    # libraries used, comma delimited
    nir.link.names = z

3. Now in your native "glue" code add the following. The macro is named
``SCALANATIVE_LINK_`` plus the uppercased name of the library.

.. code-block:: c

    #ifdef SCALANATIVE_LINK_Z

    #include <zlib.h>

    int scalanative_z_no_flush() { return Z_NO_FLUSH; }
    // other functions

    #endif

The feature works by querying the NIR code to see if the user code is using the
``z`` library. If used, ``-DSCALANATIVE_LINK_Z`` is passed to the compiler
and your "glue" code is then compiled. Otherwise, the macro keeps the code
inside from compiling. The project dependencies with native code are compiled
individually so this feature only applies to the current library being compiled.

Conceivably, another dependency could fail if this feature is not used which
could fail the whole build. The users of the native libraries should install the
required libraries they are using. This feature can make the dependency optional
if not used.

There are other valid use cases where this feature is needed. For example,
when Scala Native libraries use more that one native library but all the native
libraries do not have to be used. This allows the users to only install the
libraries they actually need for their particular application.

Continue to :ref:`testing`.
