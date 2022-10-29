package scala.scalanative.build
import java.io.{ByteArrayOutputStream, File, ObjectOutputStream, PrintWriter}
import java.nio.ByteBuffer
import java.nio.file.{Path, Paths}
import scala.collection.concurrent.TrieMap
import scala.io.Source
import scala.language.implicitConversions
import scala.scalanative.nir.Defn

class IncCompilationContext(workDir: Path) {
  private val package2hash: TrieMap[String, Long] = TrieMap[String, Long]()
  private val pack2hashPrev: TrieMap[String, Long] = TrieMap[String, Long]()
  private val changed: TrieMap[String, Long] = TrieMap[String, Long]()
  private val dumpChanged: Path = workDir.resolve(Paths.get("changed"))
  private val dumpPackage2hash: Path =
    workDir.resolve(Paths.get("package2hash"))

  def collectFromPreviousState(): Unit = {
    if (new java.io.File(dumpPackage2hash.toUri).exists) {
      val resultPrev = Source.fromFile(dumpPackage2hash.toUri)
      resultPrev.getLines().toList.foreach { vec =>
        vec.split(',') match {
          case Array(packageName, hashCodeString) =>
            pack2hashPrev.put(packageName, hashCodeString.toLong)
          case _ => // ignore
        }
      }
    }
  }

  def addEntry(packageName: String, defns: Seq[Defn]): Unit = {
    if (!package2hash.contains(packageName)) {
      val hash =
        defns.foldLeft(0)((prevhash, defn) => prevhash + defn.hashCode())
      package2hash.put(packageName, hash)
      if (!pack2hashPrev
            .contains(packageName) || pack2hashPrev(packageName) != hash) {
        changed.put(packageName, hash)
      }
    }
  }

  def shouldCompile(workdir: Path, in: Path): Boolean = {
    val packageName = workdir
      .relativize(in)
      .toString
      .replace(File.separator, ".")
      .split('.')
      .init
      .mkString(".")
    shouldCompile(packageName)
  }

  def shouldCompile(packageName: String): Boolean = {
    pack2hashPrev.isEmpty || changed.contains(packageName)
  }

  def dump(): Unit = {
    // dump the result in the current execution
    val pwHash = new PrintWriter(
      new File(dumpPackage2hash.toUri)
    )
    // This file is used for debug.
    val pwChanged = new PrintWriter(
      new File(dumpChanged.toUri)
    )
    try {
      package2hash.foreach {
        case (pack, name) => pwHash.write(pack + "," + name + "\n")
      }
      changed.foreach { case (pack, _) => pwChanged.write(pack + "\n") }
    } finally {
      pwHash.close()
      pwChanged.close()
    }
  }

  def clear(): Unit = {
    package2hash.clear()
    pack2hashPrev.clear()
    changed.clear()
  }
}
