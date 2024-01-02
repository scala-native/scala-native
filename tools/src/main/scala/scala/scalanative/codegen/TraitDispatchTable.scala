package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.linker.{Method, Trait, Class}

class TraitDispatchTable(meta: Metadata) {

  val dispatchName =
    nir.Global
      .Top("__scalanative_metadata")
      .member(nir.Sig.Generated("dispatch_table"))
  val dispatchVal = nir.Val.Global(dispatchName, nir.Type.Ptr)
  var dispatchTy: nir.Type = _
  var dispatchDefn: nir.Defn = _
  var dispatchOffset: mutable.Map[Int, Int] = _

  val traitSigIds = {
    // Collect signatures of trait methods, excluding
    // the ones defined on java.lang.Object, those always
    // go through vtable dispatch.
    val sigs = mutable.Set.empty[nir.Sig]
    meta.traits.foreach { trt =>
      trt.calls.foreach { sig =>
        if (trt.targets(sig).size > 1) {
          sigs += sig
        }
      }
    }

    val Object = meta.analysis.infos(nir.Rt.Object.name).asInstanceOf[Class]
    sigs --= Object.calls

    sigs.toArray.sortBy(_.toString).zipWithIndex.toMap
  }

  val traitClassIds = {
    def isActive(trt: Trait): Boolean =
      trt.calls.exists { sig =>
        traitSigIds.contains(sig)
      } || trt.traits.exists(isActive)

    val activeTraits =
      meta.traits.filter(isActive).toSet

    def implementsTrait(cls: Class): Boolean =
      cls.traits.exists(activeTraits.contains(_)) || cls.parent.exists(
        implementsTrait
      )
    def include(cls: Class): Boolean =
      cls.allocated && implementsTrait(cls)

    meta.classes
      .filter(include)
      .zipWithIndex
      .toMap
  }

  initDispatch()

  def initDispatch(): Unit = {
    val sigs = traitSigIds
    val sigsLength = traitSigIds.size
    val classes = traitClassIds
    val classesLength = traitClassIds.size
    val table =
      Array.fill[nir.Val](classesLength * sigsLength)(nir.Val.Null)
    val mins = Array.fill[Int](sigsLength)(Int.MaxValue)
    val maxs = Array.fill[Int](sigsLength)(Int.MinValue)
    val sizes = Array.fill[Int](sigsLength)(0)

    def put(cls: Int, meth: Int, value: nir.Val) = {
      table(meth * classesLength + cls) = value
      mins(meth) = mins(meth) min cls
      maxs(meth) = maxs(meth) max cls
      sizes(meth) = maxs(meth) - mins(meth) + 1
    }
    def get(cls: Int, meth: Int) =
      table(meth * classesLength + cls)

    // Visit every class and enter all the trait sigs they support
    classes.foreach {
      case (cls, clsId) =>
        sigs.foreach {
          case (sig, sigId) =>
            cls.resolve(sig).foreach { impl =>
              val info = meta.analysis.infos(impl).asInstanceOf[Method]
              put(clsId, sigId, info.value)
            }
        }
    }

    val (compressed, offsets) = compressTable(table, mins, sizes)

    val value = nir.Val.ArrayValue(nir.Type.Ptr, compressed.toSeq)

    dispatchOffset = offsets
    dispatchTy = nir.Type.Ptr
    dispatchDefn =
      nir.Defn.Const(nir.Attrs.None, dispatchName, value.ty, value)(
        nir.SourcePosition.NoPosition
      )
  }

  // Generate a compressed representation of the dispatch table
  // that displaces method rows one of top of the other to miniminize
  // number of nulls in the table.
  def compressTable(
      table: Array[nir.Val],
      mins: Array[Int],
      sizes: Array[Int]
  ): (Array[nir.Val], mutable.Map[Int, Int]) = {
    val classesLength = traitClassIds.size
    val sigsLength = traitSigIds.size
    val maxSize = sizes.max
    val totalSize = sizes.sum

    val free = Array.fill[List[Int]](maxSize + 1)(Nil)
    val offsets = mutable.Map.empty[Int, Int]
    val compressed = new Array[nir.Val](totalSize)
    var current = 0

    def updateFree(from: Int, total: Int): Unit = {
      var start = -1
      var size = 0
      var i = 0
      while (i < total) {
        val isNull = compressed(from + i) eq nir.Val.Null
        val inFree = start != -1
        if (inFree) {
          if (isNull) {
            size += 1
          } else {
            free(size) = start :: free(size)
            start = -1
            size = 0
          }
        } else {
          if (isNull) {
            start = from + i
            size = 1
          } else {
            ()
          }
        }
        i += 1
      }
    }

    def findFree(size: Int): Option[Int] = {
      var bucket = size
      while (bucket <= maxSize) {
        if (free(bucket).nonEmpty) {
          val head :: tail = free(bucket): @unchecked
          free(bucket) = tail
          val leftoverSize = bucket - size
          if (leftoverSize != 0) {
            val leftoverStart = head + size
            free(leftoverSize) = leftoverStart :: free(leftoverSize)
          }
          return Some(head)
        }
        bucket += 1
      }
      None
    }

    def allocate(sig: Int): Int = {
      val start = mins(sig)
      val size = sizes(sig)
      val offset =
        findFree(size).getOrElse {
          val offset = current
          current += sizes(sig)
          offset
        }
      java.lang.System.arraycopy(
        table,
        sig * classesLength + start,
        compressed,
        offset,
        size
      )
      updateFree(offset, size)
      offset
    }

    sizes.zipWithIndex.sortBy(-_._1).foreach {
      case (_, sig) =>
        offsets(sig) = allocate(sig) - mins(sig)
    }

    val result = new Array[nir.Val](current)
    System.arraycopy(compressed, 0, result, 0, current)

    (result, offsets)
  }

}
