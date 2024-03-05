package scala.scalanative
package codegen

import java.io.{ByteArrayOutputStream, File, ObjectOutputStream, PrintWriter}
import java.nio.ByteBuffer
import java.nio.file.{Path, Paths, Files}
import scala.collection.concurrent.TrieMap
import scala.io.Source
import scala.language.implicitConversions
import scala.scalanative.build.Build

private[codegen] class IncrementalCodeGenContext(config: build.Config) {
  private val package2hash: TrieMap[String, Long] = TrieMap[String, Long]()
  private val pack2hashPrev: TrieMap[String, Long] = TrieMap[String, Long]()
  private val changed: TrieMap[String, Long] = TrieMap[String, Long]()
  private val dumpPackage2hash: Path = config.workDir.resolve("package2hash")

  def collectFromPreviousState(): Unit = {
    if (Build.userConfigHasChanged(config))
      Files.deleteIfExists(dumpPackage2hash)
    else if (Files.exists(dumpPackage2hash)) {
      Source
        .fromFile(dumpPackage2hash.toUri())
        .getLines()
        .toList
        .foreach { vec =>
          vec.split(',') match {
            case Array(packageName, hashCodeString) =>
              pack2hashPrev.put(packageName, hashCodeString.toLong)
            case _ => // ignore
          }
        }
    }
  }

  def addEntry(packageName: String, defns: Seq[nir.Defn]): Unit = {
    val hash = defns.foldLeft(0L)(_ + _.hashCode())
    val prevHash = pack2hashPrev.get(packageName)
    package2hash.put(packageName, hash)
    if (prevHash.forall(_ != hash)) {
      changed.put(packageName, hash)
    }
  }

  def shouldCompile(packageName: String): Boolean =
    changed.contains(packageName)

  def dump(): Unit = {
    // dump the result in the current execution
    val pwHash = new PrintWriter(dumpPackage2hash.toFile())
    try
      package2hash.foreach {
        case (packageName, hash) =>
          pwHash.write(packageName)
          pwHash.write(",")
          pwHash.println(hash)
      }
    finally pwHash.close()
  }

  def clear(): Unit = {
    package2hash.clear()
    pack2hashPrev.clear()
    changed.clear()
  }
}
