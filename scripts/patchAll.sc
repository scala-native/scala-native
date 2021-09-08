import $ivy.`com.lihaoyi::ammonite-ops:2.3.8`, ammonite.ops._, mainargs._
import $file.`scalalib-patch-tool`

for {
  version <- List(
    "2.13.6",
    "2.13.4",
    "2.12.14",
    "2.12.13",
    "2.11.12"
  )
  cmd <- List("recreate", "create", "prune")
  _ = println(s"$cmd $version")
  res = %%("amm", "scripts/scalalib-patch-tool.sc", cmd, version)(pwd)
} {
  println(res)
}

for {
  version <- List(
    "2.12.14"
  )
  cmd <- List("recreate", "create", "prune")
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
