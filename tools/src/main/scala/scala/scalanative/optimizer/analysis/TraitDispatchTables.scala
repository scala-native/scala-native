package scala.scalanative
package optimizer
package analysis

import scala.collection.mutable
import ClassHierarchy._
import nir._

class TraitDispatchTables(top: Top) {
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

  val traitMethods = top.methods.filter(_.inTrait).sortBy(_.id)
  val traitMethodSigs = {
    val sigs = mutable.Map.empty[String, Int]
    var i    = 0
    traitMethods.foreach { meth =>
      val sig = meth.name.id
      if (!sigs.contains(sig)) {
        sigs(sig) = i
        i += 1
      }
    }
    sigs
  }

  def initDispatch(): Unit = {
    val sigs          = traitMethodSigs
    val sigsLength    = traitMethodSigs.size
    val classes       = top.classes.sortBy(_.id)
    val classesLength = classes.length
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
    classes.foreach { cls =>
      def visit(cur: Class): Unit = {
        cur.methods.foreach { meth =>
          val sig = meth.name.id
          if (sigs.contains(sig)) {
            val id = sigs(sig)
            if (get(cls.id, id) eq Val.Null) {
              put(cls.id, id, meth.value)
            }
          }
        }
        cur.parent.foreach(visit)
      }
      visit(cls)
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
    row(trt.id) = true
    trt.traits.foreach { right =>
      row(right.id) = true
    }
    trt.traits.foreach(markTraits(row, _))
  }

  def initClassHasTrait(): Unit = {
    val columns = top.classes.sortBy(_.id).map { cls =>
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
    val columns = top.traits.sortBy(_.id).map { left =>
      val row = new Array[Boolean](top.traits.length)
      markTraits(row, left)
      row(left.id) = true
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
