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

EXPERIMENTAL: Deployment Descriptor for passing settings to the compiler
========================================================================

These are **experimental** features that were added because they are used
internally by Scala Native to simplify the build and organize the native
code with their respective projects. These features allow a library
developer that has native code included with their project to have
better control over compilation settings used for their project. By
adding a ``scala-native.properties`` file in the root of your project's
``resources/scala-native`` directory, settings can be added to the
properties file that are added to the compile command.

These features allow the settings described below to apply only to your
library during compilation.

Use the following procedure to use any of the features described below.

* Add a Scala Native deployment descriptor to your library.The properties file
  must be named ``scala-native.properties`` and must be put in the base of the
  ``src/main/resources/scala-native`` directory.

Optional compilation of code if ``@link`` is found
--------------------------------------------------

Libraries developed with "glue" code as described in the previous section
can cause compilation errors when all the following conditions occur:

1. The library and/or header files are not installed

2. The dependency is in the library users' build

3. The code that uses the "glue" code is not called by the application
   or library

If the glue "code" is being called, then the library and headers need to
be installed to compile your application otherwise errors are expected.

Scala Native code can include the annotation ``@link("z")`` for example
that says link with the ``z`` library. The compiler will add a link
option ``-lz`` for this library to the linking phase of the build if the code
with the annotation is used. See :ref:`interop`,
`Linking with native libraries` section for more information.

This **experimental** feature has been added so the users of your published
library can avoid the error described above. Use the following procedure to
implement this feature.

1. Add the following content to your new ``scala-native.properties`` file
desdribed above. For the purposes of this example assume the library is ``z``.
Note that if your library has more that one library you can add a comma
delimited list of libraries. If desired, the comments are not needed.

.. code-block:: properties

    # configuration for glue code
    # defines SCALANATIVE_LINK_Z if @link("z") annnotation is used (found in NIR)
    # libraries used, comma delimited
    nir.link.names = z

2. Now in your native "glue" code add the following. The macro is named
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
inside from compiling.

Adding defines to your library when code is being compiled
----------------------------------------------------------

If your library requires a C preprocessor define then use this feature to add
the define ``-DMY_DEFINE`` for example to the options passed to the compiler.

.. code-block:: properties

    # add defines, do not add -D
    preprocessor.defines = MY_DEFINE, MY_VALUE=2

Add extra include paths for your library
----------------------------------------

Currently, the native code compilation provides an include to your
project's ``resources/scala-native`` directory. This means
that code needs to use relative includes. e.g. ``#include "mylib.h"``
The build scans for all files to compile so only relative paths are
needed from your base ``scala-native`` directory

This feature allows you to vendor code, include code as is, that has
system includes. e.g. ``#include <libunwind.h>`` Add the path
starting from the ``scala-native`` path shown above. If you have a more
complex setup, you could also put your code in subdirectories and add
paths to them. Add the paths in Linux/UNIX style and they will be
converted as needed on the Windows platform.

.. code-block:: properties

    # path to vendored libunwind a base gc path
    compile.include.paths = platform/posix/libunwind, gc


Add unique identity to your library for debugging
-------------------------------------------------

Since these features can apply to libraries that are published,
those coordinates can be used to identify your library. The
example here is for a Scala Native ``javalib`` library.

.. code-block:: properties

    # output via debugging
    project.organization = org.scala-native
    project.name = javalib

The descriptor and its settings are printed when compiling
in debug mode. Use the following command if using `sbt`:

.. code-block:: sh

    sbt --debug

Other **experimental** features may be added for new requirements.

Continue to :ref:`testing`.
