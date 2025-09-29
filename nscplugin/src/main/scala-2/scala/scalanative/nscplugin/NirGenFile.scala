package scala.scalanative
package nscplugin

import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.{Path, Paths}

import scala.tools.nsc.Global
import scala.tools.nsc.io.AbstractFile

trait NirGenFile[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import global._

  def genPathFor(
      cunit: CompilationUnit,
      ownerName: nir.Global
  ): AbstractFile = {
    val nir.Global.Top(id) = ownerName
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val pathParts = id.split("[./]")
    val dir = pathParts.init.foldLeft(baseDir)(_.subdirectoryNamed(_))

    val filename = pathParts.last
    dir.fileNamed(filename + ".nir")
  }

  def genIRFile(path: AbstractFile, defns: Seq[nir.Defn]): Unit = {
    val channel = Channels.newChannel(path.bufferedOutput)
    try nir.serialization.serializeBinary(defns, channel)
    finally channel.close()
  }
}
