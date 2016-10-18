package scala.scalanative
package compiler
package pass

import nir._, Shows._
import util.sh
import scala.util.hashing.MurmurHash3

object GlobalValueNumbering {

  type Hash = Int

  class HashFunction(hashLocal: Local => Hash) extends (Any => Hash) {

    import HashFunction._

    def apply(obj: Any): Hash = {
      obj match {
        case op: Op     => hashOp(op)
        case value: Val => hashVal(value)

        case local: Local => hashLocal(local)

        case ty: Type   => hashType(ty)
        case g: Global  => hashGlobal(g)
        case bin: Bin   => hashBin(bin)
        case comp: Comp => hashComp(comp)
        case conv: Conv => hashConv(conv)

        case b: Boolean  => b.hashCode
        case i: Int      => i.hashCode
        case d: Double   => d.hashCode
        case str: String => str.hashCode

        case _ =>
          throw new IllegalArgumentException(
            s"Unable to hash value {${obj}} of type ${obj.getClass.getName}")
      }
    }

    def hashOp(op: Op): Hash = {
      import Op._
      val opFields: Seq[Any] = op match {
        case Call(ty, ptr, args)    => "Call" +: ty +: ptr +: args
        case Load(ty, ptr)          => Seq("Load", ty, ptr)
        case Store(ty, ptr, value)  => Seq("Store", ty, ptr, value)
        case Elem(ty, ptr, indexes) => "Elem" +: ty +: ptr +: indexes
        case Extract(aggr, indexes) => "Extract" +: aggr +: indexes
        case Insert(aggr, value, indexes) =>
          "Insert" +: aggr +: value +: indexes

        case Stackalloc(ty, n)          => Seq("Stackalloc", ty, n)
        case Bin(bin, ty, l, r)         => Seq("Bin", bin, ty, l, r)
        case Comp(comp, ty, l, r)       => Seq("Comp", comp, ty, l, r)
        case Conv(conv, ty, value)      => Seq("Conv", ty, value)
        case Select(cond, thenv, elsev) => Seq("Select", cond, thenv, elsev)

        case Field(ty, obj, name)       => Seq("Field", obj, name)
        case Method(ty, obj, name)      => Seq("Method", ty, obj, name)
        case As(ty, obj)                => Seq("As", ty, obj)
        case Is(ty, obj)                => Seq("Is", ty, obj)
        case Copy(value)                => Seq("Copy", value)
        case Closure(ty, fun, captures) => "Closure" +: ty +: fun +: captures

        case Classalloc(name) => Seq("Classalloc", name)
        case Module(name)     => Seq("Module", name)
        case Sizeof(ty)       => Seq("Sizeof", ty)
      }

      combineHashes(opFields.map(this.apply))
    }

    def hashVal(value: Val): Hash = {
      import Val._
      val fields: Seq[Any] = value match {
        case Struct(name, values)  => "Struct" +: name +: values
        case Array(elemty, values) => "Array" +: elemty +: values
        case Const(value)          => Seq("Const", value)

        case Local(name, _) => Seq(hashLocal(name))

        // the other val kinds can't have another Val in them
        case _ => Seq(value.hashCode)
      }

      combineHashes(fields.map(this.apply))
    }

    def hashType(ty: Type): Hash = {
      ty.hashCode
    }

    def hashGlobal(global: Global): Hash = {
      global.hashCode
    }

    def hashBin(bin: Bin): Hash = {
      bin.hashCode
    }

    def hashComp(comp: Comp): Hash = {
      comp.hashCode
    }

    def hashConv(conv: Conv): Hash = {
      conv.hashCode
    }

  }

  object HashFunction {

    def combineHashes(hashes: Seq[Hash]): Hash = {
      MurmurHash3.orderedHash(hashes)
    }

    def rawLocal(local: Local): Hash = {
      combineHashes(Seq(local.scope.hashCode, local.id.hashCode))
    }

  }

}
