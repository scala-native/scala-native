package scala.scalanative.sbtplugin

import java.nio.file.{Files, Path}
import java.util.regex.Pattern

import scala.collection.mutable.ArrayBuffer

import xsbti.compile.AuxiliaryClassFiles

/** NIR products must follow their JVM class through incremental recompilation,
 *  source deletion, and transactional rollback.
 */
private[sbtplugin] object NirAuxiliaryClassFiles extends AuxiliaryClassFiles {
  private final val LambdaSuffix = """\$\$Lambda\$[0-9]+"""
  private final val ReflectionSuffix =
    """(?:\$[0-9]+)?\$(?:SN|scalanative)\$ReflectivelyInstantiate\$"""
  private val GeneratedSuffixPattern =
    Pattern.compile(raw"""(?:$LambdaSuffix|$ReflectionSuffix)\.nir""")

  override def associatedFiles(classFile: Path): Array[Path] = {
    val filename = classFile.getFileName.toString
    if (!filename.endsWith(".class")) Array.empty
    else {
      val name = filename.stripSuffix(".class")
      val nirFile = classFile.resolveSibling(name + ".nir")
      val directory = classFile.getParent
      if (!Files.isDirectory(directory)) Array(nirFile)
      else {
        // These classes are generated only by the Native backend, so Zinc
        // cannot discover them through the JVM compiler's products.
        val files = Files.list(directory)
        try {
          val associated = ArrayBuffer(nirFile)
          val iterator = files.iterator()
          while (iterator.hasNext) {
            val path = iterator.next()
            val filename = path.getFileName.toString
            if (filename.startsWith(name) &&
                GeneratedSuffixPattern
                  .matcher(filename.substring(name.length))
                  .matches())
              associated += path
          }
          associated.toArray
        } finally files.close()
      }
    }
  }
}
