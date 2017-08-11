package scala.scalanative
package nir

import scala.collection.mutable

class DefnBuffer {
  private val buffer       = mutable.UnrolledBuffer.empty[Defn]
  def toSeq: Seq[Defn]     = buffer
  def +=(defn: Defn): Unit = buffer += defn
}
