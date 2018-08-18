package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.sema._

class TraitDispatchTables(meta: Metadata, top: Top) {
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
    top.traits.foreach { trt =>
      trt.calls.foreach { sig =>
        if (top.targets(Type.Trait(trt.name), sig).size > 1) {
          sigs += sig
        }
      }
    }
    val Object = top.nodes(Rt.Object.name).asInstanceOf[Class]
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
      top.traits.filter(isActive).toSet

    def implementsTrait(cls: Class): Boolean =
      cls.traits.exists(activeTraits.contains(_)) || cls.parent.exists(
        implementsTrait)
    def include(cls: Class): Boolean =
      cls.allocated && implementsTrait(cls)

    top.classes.filter(include).sortBy(meta.ids(_)).zipWithIndex.toMap
  }

  def initDispatch(): Unit = {
    val sigs          = traitSigIds
    val sigsLength    = traitSigIds.size
    val classes       = traitClassIds
    val classesLength = traitClassIds.size
    val table =
      Array.fill[Val](classesLength * sigsLength)(Val.Null)
    val mins = Array.fill[Int](sigsLength)(Int.MaxValue)
    val maxs = Array.fill[Int](sigsLength)(Int.MinValue)

    def put(cls: Int, meth: Int, value: Val) = {
      table(meth * classesLength + cls) = value
      mins(meth) = mins(meth) min cls
      maxs(meth) = maxs(meth) max cls
    }
    def get(cls: Int, meth: Int) =
      table(meth * classesLength + cls)

    // Visit every class and enter all the trait sigs they support
    classes.foreach {
      case (cls, clsId) =>
        sigs.foreach {
          case (sig, sigId) =>
            cls.resolve(sig).foreach { impl =>
              put(clsId, sigId, impl.value)
            }
        }
    }

    // Generate a compressed representation of the dispatch table
    // that displaces method rows one of top of the other to miniminize
    // number of nulls in the table.
    val offsets = mutable.Map.empty[Int, Int]
    val compressed = new Array[Val]({
      var meth = 0
      var size = 0
      while (meth < sigsLength) {
        val min = mins(meth)
        if (min != Int.MaxValue) {
          val max = maxs(meth)
          size += max - min + 1
        }
        meth += 1
      }
      size
    })
    var current = 0
    var meth    = 0
    while (meth < sigsLength) {
      val start = mins(meth)
      val end   = maxs(meth)
      if (start == Int.MaxValue) {
        offsets(meth) = 0
      } else {
        val total = end - start + 1
        java.lang.System.arraycopy(table,
                                   meth * classesLength + start,
                                   compressed,
                                   current,
                                   total)
        offsets(meth) = current - start
        current += total
      }
      meth += 1
    }

    val value = Val.Array(Type.Ptr, compressed)

    dispatchOffset = offsets
    dispatchTy = Type.Ptr
    dispatchDefn = Defn.Const(Attrs.None, dispatchName, value.ty, value)
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
    val columns = top.classes.sortBy(meta.ids(_)).map { cls =>
      val row = new Array[Boolean](top.traits.length)
      markTraits(row, cls)
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, top.traits.length), columns)

    classHasTraitTy = table.ty
    classHasTraitDefn =
      Defn.Const(Attrs.None, classHasTraitName, table.ty, table)
  }

  def initTraitHasTrait(): Unit = {
    val columns = top.traits.sortBy(meta.ids(_)).map { left =>
      val row = new Array[Boolean](top.traits.length)
      markTraits(row, left)
      row(meta.ids(left)) = true
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, top.traits.length), columns)

    traitHasTraitTy = table.ty
    traitHasTraitDefn =
      Defn.Const(Attrs.None, traitHasTraitName, table.ty, table)
  }

  initDispatch()
  initClassHasTrait()
  initTraitHasTrait()
}
