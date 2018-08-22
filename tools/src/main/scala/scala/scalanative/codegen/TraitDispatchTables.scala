package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker.{Trait, Class}

class TraitDispatchTables(meta: Metadata) {
  val linked = meta.linked

  val dispatchName                          = Global.Top("__dispatch")
  val dispatchVal                           = Val.Global(dispatchName, Type.Ptr)
  var dispatchTy: Type                      = _
  var dispatchDefn: Defn                    = _
  var dispatchOffset: mutable.Map[Int, Int] = _

  val classHasTraitName       = Global.Top("__class_has_trait")
  val classHasTraitVal        = Val.Global(classHasTraitName, Type.Ptr)
  var classHasTraitTy: Type   = _
  var classHasTraitDefn: Defn = _

  val traitHasTraitName       = Global.Top("__trait_has_trait")
  val traitHasTraitVal        = Val.Global(traitHasTraitName, Type.Ptr)
  var traitHasTraitTy: Type   = _
  var traitHasTraitDefn: Defn = _

  val traitSigIds = {
    // Collect signatures of trait methods, excluding
    // the ones defined on java.lang.Object, those always
    // go through vtable dispatch.
    val sigs = mutable.Set.empty[String]
    linked.traits.foreach { trt =>
      trt.calls.foreach { sig =>
        if (linked.targets(Type.Trait(trt.name), sig).size > 1) {
          sigs += sig
        }
      }
    }
    val Object = linked.infos(Rt.Object.name).asInstanceOf[Class]
    sigs.toList.foreach { sig =>
      if (Object.calls.contains(sig)) {
        sigs -= sig
      }
    }
    sigs.toArray.sorted.zipWithIndex.toMap
  }

  val traitClassIds = {
    def isActive(trt: Trait): Boolean =
      trt.calls.exists { sig =>
        traitSigIds.contains(sig)
      } || trt.traits.exists(isActive)

    val activeTraits =
      linked.traits.filter(isActive).toSet

    def implementsTrait(cls: Class): Boolean =
      cls.traits.exists(activeTraits.contains(_)) || cls.parent.exists(
        implementsTrait)
    def include(cls: Class): Boolean =
      cls.allocated && implementsTrait(cls)

    linked.classes
      .filter(include)
      .toArray
      .sortBy(meta.ids(_))
      .zipWithIndex
      .toMap
  }

  initDispatch()
  initClassHasTrait()
  initTraitHasTrait()

  def initDispatch(): Unit = {
    val sigs          = traitSigIds
    val sigsLength    = traitSigIds.size
    val classes       = traitClassIds
    val classesLength = traitClassIds.size
    val table =
      Array.fill[Val](classesLength * sigsLength)(Val.Null)
    val mins  = Array.fill[Int](sigsLength)(Int.MaxValue)
    val maxs  = Array.fill[Int](sigsLength)(Int.MinValue)
    val sizes = Array.fill[Int](sigsLength)(0)

    def put(cls: Int, meth: Int, value: Val) = {
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
              put(clsId, sigId, Val.Global(impl, Type.Ptr))
            }
        }
    }

    val (compressed, offsets) = compressTable(table, mins, sizes)

    val value = Val.Array(Type.Ptr, compressed)

    dispatchOffset = offsets
    dispatchTy = Type.Ptr
    dispatchDefn = Defn.Const(Attrs.None, dispatchName, value.ty, value)
  }

  // Generate a compressed representation of the dispatch table
  // that displaces method rows one of top of the other to miniminize
  // number of nulls in the table.
  def compressTable(table: Array[Val],
                    mins: Array[Int],
                    sizes: Array[Int]): (Array[Val], mutable.Map[Int, Int]) = {
    val classesLength = traitClassIds.size
    val sigsLength    = traitSigIds.size
    val maxSize       = sizes.max
    val totalSize     = sizes.sum

    val free       = Array.fill[List[Int]](maxSize + 1)(List())
    val offsets    = mutable.Map.empty[Int, Int]
    val compressed = new Array[Val](totalSize)
    var current    = 0

    def updateFree(from: Int, total: Int): Unit = {
      var start = -1
      var size  = 0
      var i     = 0
      while (i < total) {
        val isNull = compressed(from + i) eq Val.Null
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
          val head :: tail = free(bucket)
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
      val size  = sizes(sig)
      val offset =
        findFree(size).getOrElse {
          val offset = current
          current += sizes(sig)
          offset
        }
      java.lang.System.arraycopy(table,
                                 sig * classesLength + start,
                                 compressed,
                                 offset,
                                 size)
      updateFree(offset, size)
      offset
    }

    sizes.zipWithIndex.sortBy(-_._1).foreach {
      case (_, sig) =>
        offsets(sig) = allocate(sig) - mins(sig)
    }

    val result = new Array[Val](current)
    System.arraycopy(compressed, 0, result, 0, current)

    (result, offsets)
  }

  def markTraits(row: Array[Boolean], cls: Class): Unit = {
    cls.traits.foreach(markTraits(row, _))
    cls.parent.foreach(markTraits(row, _))
  }

  def markTraits(row: Array[Boolean], trt: Trait): Unit = {
    row(meta.ids(trt)) = true
    trt.traits.foreach { right =>
      row(meta.ids(right)) = true
    }
    trt.traits.foreach(markTraits(row, _))
  }

  def initClassHasTrait(): Unit = {
    val columns = linked.classes.toArray.sortBy(meta.ids(_)).map { cls =>
      val row = new Array[Boolean](linked.traits.length)
      markTraits(row, cls)
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, linked.traits.length), columns)

    classHasTraitTy = table.ty
    classHasTraitDefn =
      Defn.Const(Attrs.None, classHasTraitName, table.ty, table)
  }

  def initTraitHasTrait(): Unit = {
    val columns = linked.traits.toArray.sortBy(meta.ids(_)).map { left =>
      val row = new Array[Boolean](linked.traits.length)
      markTraits(row, left)
      row(meta.ids(left)) = true
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, linked.traits.length), columns)

    traitHasTraitTy = table.ty
    traitHasTraitDefn =
      Defn.Const(Attrs.None, traitHasTraitName, table.ty, table)
  }
}
