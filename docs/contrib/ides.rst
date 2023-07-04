.. _ides:

Metals
======
Metals import should work out of the box for most of the modules, it's the recommended IDE.  
To speed up indexing and prevent Bloop-related issues by default we export only 1 version of ``MultiScalaProject``, otherwise it would need to cross-compile sources for all binary Scala versions on each source-change.
By default IDE would target Scala 3 projects, to change this behavior modify ``project/MyScalaNativePlugin.scala`` and modify ``ideScalaVersion``. This change would be only required when developing Scala 2 compiler plugins, sbt plugins or Scala 2 specific sources.


IntelliJ IDEA
=============

* Select "Create project from existing sources" and choose the ``build.sbt`` file. When prompted, select "Open as project". Make sure you select the "Use sbt shell" for both import and build.

* When the import is complete, we need to fix some module dependencies:

  * ``scalalib``: Right-click on the module, "Mark directory as" -> "Excluded". This is needed because ``scalalib`` is only meant to be used at runtime (it is the Scala library that the executables link against). Not excluding it makes IDEA think that the Scala library comes from it, which results into highlighting errors.
  * ``nscplugin``: We need to add what SBT calls ``unmanagedSourceDirectories`` as dependencies. Go go Project Structure -> Modules -> ``nscplugin`` -> Dependencies and click the + icon. Select "JARs or Directories" and navigate to the ``nir`` directory at the root of the Scala Native project. Repeat for the ``util`` directory.
  * ``native-build``: We need to add the ``sbt-scala-native`` module as a dependency. Go go Project Structure -> Modules -> ``native-build`` -> Dependencies and click the + icon. Select "Module Dependency" and select the ``sbt-scala-native`` module.

The above is not an exhaustive list, but it is the bare minimum to have the build working. Please keep in mind that you will have to repeat the above steps, in case you reload (re-import) the SBT build. This will need to happen if you change some SBT-related file (e.g. ``build.sbt``).

