package scala.scalanative

package nscplugin

import scala.collection.mutable
import scala.scalanative.nir._

class ReflectiveInstantiationBuffer(val fqcn: String) {
  private val buf = mutable.UnrolledBuffer.empty[nir.Defn]

  def +=(defn: nir.Defn): Unit = {
    buf += defn
  }

  val name = nir.Global.Top("SN$ReflectivelyInstantiate$" + fqcn)

  def nonEmpty = buf.nonEmpty
  def toSeq    = buf.toSeq
}

object ReflectiveInstantiationInfo {
  private val bufs =
    mutable.UnrolledBuffer.empty[ReflectiveInstantiationBuffer]

  def +=(buf: ReflectiveInstantiationBuffer): Unit = {
    bufs += buf
  }

  def nonEmpty = bufs.nonEmpty
  def toSeq    = bufs.toSeq
  def last     = bufs.last

  def foreach(f: ReflectiveInstantiationBuffer => Unit): Unit = {
    bufs.foreach(f)
  }
}
