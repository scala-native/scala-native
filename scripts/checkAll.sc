import $ivy.`com.lihaoyi::ammonite-ops:2.3.8`, ammonite.ops._, mainargs._

import java.lang._
val cmds = for {
  version <- List(
    "2.13.6",
    "2.13.5",
    "2.13.4",
    "2.12.14",
    "2.12.13",
    "2.11.12"
  )
} yield s"++ $version;scalalib/compile"

new ProcessBuilder(Seq("sbt", cmds.mkString(";")):_*).inheritIO.start.waitFor
