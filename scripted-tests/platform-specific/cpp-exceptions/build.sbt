enablePlugins(ScalaNativePlugin)

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

// Selects the C++ exception backend, as used in LTO.
// Throwing then goes through the C++ runtime's __cxa_throw, so the unwinder
// has to step out of a frame belonging to libstdc++ or libc++ rather than only
// out of code we compiled ourselves. Using -fcxx-exceptions rather than LTO
// keeps the test cheap and avoids LTO's own platform problems.
nativeConfig ~= { c =>
  c.withCppOptions(c.cppOptions :+ "-fcxx-exceptions")
}
