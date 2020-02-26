package scala.scalanative

package nscplugin

import scala.collection.mutable
import scala.scalanative.nir._

class ReflectiveInstantiationInfo {
    private val buf = mutable.UnrolledBuffer.empty[nir.Defn]

    def +=(defn: nir.Defn): Unit = {
        buf += defn
    }

    val name = nir.Global.Top("SN$LazyModuleLoaders__$")

    def nonEmpty = buf.nonEmpty
    def toSeq = buf.toSeq

    def toSeqCompleted: Seq[nir.Defn] = {
        buf :+ nir.Defn.Module(Attrs(), name, None, Seq())
    }
}