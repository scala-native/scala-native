package scala.scalanative

package nscplugin

import scala.collection.mutable

class ReflectiveInstantiationBuffer(val fqcn: String) {

  private val buf = mutable.UnrolledBuffer.empty[nir.Defn]

  def +=(defn: nir.Defn): Unit = {
    buf += defn
  }

  val name = nir.Global.Top(fqcn + "$SN$ReflectivelyInstantiate$")

  def nonEmpty = buf.nonEmpty

  def toSeq = buf.toSeq

}

object ReflectiveInstantiationBuffer {

  def apply(fqcn: String) = new ReflectiveInstantiationBuffer(fqcn)

}
