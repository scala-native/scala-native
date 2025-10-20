package scala.scalanative

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.io.*
import scala.scalanative.util.Scope

package object linker {
  def compileAndLoad(
      sources: (String, String)*
  )(fn: Seq[nir.Defn] => Unit): Unit = {
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources.toMap)
      val dir = VirtualDirectory.real(outDir)

      val defns = compiler
        .compile(sourcesDir)
        .toSeq
        .filter(_.toString.endsWith(".nir"))
        .map(outDir.relativize(_))
        .flatMap { path =>
          nir.serialization.deserializeBinary(dir, path)
        }
      fn(defns)
    }
  }
}
