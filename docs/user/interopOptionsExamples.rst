.. _interopOptionsExamples:

Compiler & Linker Option Examples 
=================================

Scala Native provides default compiler and linker options selected to cover
a wide variety of situations.  Sometimes it is useful to:

#. See what programs and options are actively being used. 
   
#. Change the option to something more suitable for the local or
   target environment.

The sbt `++=` appends the right hand sequence to the previous value of
the left hand setting.

Options are passed to the compiler or linker as the trailing or
right-most part of the command line. Most compilers
and linkers follow the convention that later options override or
supersede any early specification of the same option.

The following examples are, for simplicity, presented separately. They
can often be combined, either as separate lines or as one Seq().

Examples were tested on sbt version 0.13.7. They may need
to be modified as sbt changes.

These techniques can aid but not replace well chosen and competently
implemented algorithms and data structures. 

.. highlight:: scala

.. _CompileOptions:

Using nativeCompileOptions sbt setting 
--------------------------------------

To show compiler command, when executed, verbosely::

  // Sometimes you want exposed the gears.
 
  nativeCompileOptions ++= Seq("-v")

.. _LinkerOptions:

Using nativeLinkerOptions sbt setting 
-------------------------------------

To show linker command, when executed, verbosely::

  nativeLinkingOptions ++= Seq("-v")

To link with one or more object (.o) files in (sub-) project root directory::

  // Let sbt do the heavy lifting of figuring out the path
  // to the project root directory.
  // Useful when frequently cloning "master" project templates
  // or doing cut & paste edits.

  nativeLinkingOptions ++=
    Seq(baseDirectory.value.getAbsolutePath + "/first.o")

  nativeLinkingOptions ++=
    Seq(baseDirectory.value.getAbsolutePath + "/second.o")

  // Same goal, condensed to one Seq()

  nativeLinkingOptions ++=
    Seq(baseDirectory.value.getAbsolutePath + "/first.o",
        baseDirectory.value.getAbsolutePath + "/second.o")
    
To link with one or more object (.o) files at an absolute location::

  // There are also situations where an absolute path it easier to get
  // up and running.

  nativeLinkingOptions ++=
    Seq("/mnt/projects/CObjs/third.o")

To add a path to the list of paths the linker will search for archive (static)
libraries (.a)::

  // libmylib.a is in Clib sub-directory of current project.
  // @native("mylib") in scala code. Then set linker option in .sbt.

  nativeLinkingOptions ++=
    Seq("-L" + baseDirectory.value.getAbsolutePath + "/Clib")

**This next example is based upon ideas originally presented by**
`olofwalker <https://github.com/olofwalker/scala-native-example>`_.

If the dynamic library (.so) of interest is in one of the "well known places"
for the linker, the linker will find it without explicit options.
When a project reaches a somewhat stable stage of development, it can be
useful to link with a local copy of the dynamic library. This avoids
putting development code into shared spaces and the need for elevated
privileges to write to those directories.

To add a path to the list of paths linux ld compatible linkers will search
for dynamic libraries (.so)::

  // CAUTION: static location is for early development only.
  //          For rpath link & runtime behavior, see GNU ld documentation.
  //          Other operating systems may use other methods.

  // libmylib.so is in Clib sub-directory of current project.
  // @native("mylib") in scala code. Then set two linker options in .sbt.
  // One will be used when the executable is linked. The second will be
  // used when the file is run.
   
  // This allows link to succeed.

  nativeLinkingOptions ++= Seq("-L" ++ baseDirectory.value.getAbsolutePath()
                                ++ "/Clib")

  // This allows run to succeed.
  // String below starts with  "-Well", not "-Wone". 

  nativeLinkingOptions ++= Seq("-Wl,-rpath,"
                             ++ baseDirectory.value.getAbsolutePath()
                             ++ "/Clib")

To use an alternate linker, such as LLVM lld::

  nativeLinkingOptions ++= Seq("-fuse-ld=lld")

To reduce size of the executable file by omitting all symbol information::

  // String below starts with  "-Well", not "-Wone". 

  nativeLinkingOptions ++= Seq("-Wl,--strip-all")


Continue to :ref:`lib`.
