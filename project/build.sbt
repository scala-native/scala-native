Compile / unmanagedSourceDirectories ++= {
  val root = baseDirectory.value.getParentFile

  Seq(
    "util",
    "nir",
    "tools",
    "sbt-scala-native",
    "test-interface-common",
    "test-runner"
  ).flatMap { dir =>
    Seq(
      root / s"$dir/src/main/scala",
      root / s"$dir/src/main/scala-sbt-2",
      root / s"$dir/jvm/src/main/scala"
    )
  }
}

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.5")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.8")

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "5.13.3.202401111512-r"
libraryDependencies += "me.bechberger" % "ap-loader-all" % "4.2-10"

// scalacOptions used to bootstrap to sbt prompt.
// In particular, no "-Xfatal-warnings"
// A stricter set of Options is used in the project root build.sbt.
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding:utf8",
  "-feature",
  "-unchecked"
) ++ Seq(
        // In 2.13 lineStream_! was replaced with lazyList_!.
      "method lineStream_!",
      // OpenHashMap is used with value class parameter type, we cannot replace it with AnyRefMap or LongMap
      // Should not be replaced with HashMap due to performance reasons.
      "class|object OpenHashMap",
      "class Stream",
      "method retain in trait SetOps",
      "object AnyRefMap.*Use `scala.collection.mutable.HashMap` ",

      "`= _` has been deprecated",
      "`_` is deprecated for wildcard arguments of types",
      /*The syntax `x: _* is */ "no longer supported for vararg splice",
      "The syntax `<function> _` is no longer supported",
      "Implicit parameters should be provided with a `using` clause"
    ).map(msg => s"-Wconf:msg=$msg:s")
