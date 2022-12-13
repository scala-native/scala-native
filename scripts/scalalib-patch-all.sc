import $ivy.`com.lihaoyi::ammonite-ops:2.3.8`, ammonite.ops._, mainargs._
import $file.`scalalib-patch-tool`

val crossScala212 = List("2.12.13", "2.12.14", "2.12.15")
val crossScala213 = List("2.13.4", "2.13.5", "2.13.6", "2.13.7")

val commands = List("recreate", "create", "prune")

for {
  version <- crossScala212 ++ crossScala213
  cmd <- commands
  _ = println(s"$cmd $version")
  res = %%("amm", "scripts/scalalib-patch-tool.sc", cmd, version)(pwd)
} {
  println(res)
}

for {
  version <- List(crossScala212.last)
  cmd <- commands
  _ = println(s"$cmd $version")
  res = %%(
    "amm",
    "scripts/scalalib-patch-tool.sc",
    cmd,
    version,
    "scalalib/old-collections"
  )(pwd)
} {
  println(res)
}
