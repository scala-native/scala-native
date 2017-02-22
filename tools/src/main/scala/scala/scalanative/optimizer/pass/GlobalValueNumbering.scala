package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import scala.util.hashing.MurmurHash3

import analysis.ClassHierarchy.Top
import analysis.ControlFlow
import analysis.ControlFlow.Block
import analysis.DominatorTree

import nir._

class GlobalValueNumbering extends Pass {
  import GlobalValueNumbering._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val cfg        = ControlFlow.Graph(insts)
    val domination = DominatorTree.build(cfg)

    performSimpleValueNumbering(cfg, domination)
  }

  private def performSimpleValueNumbering(
      cfg: ControlFlow.Graph,
      domination: Map[Block, Set[Block]]): Seq[Inst] = {

    val variableVN   = mutable.HashMap.empty[Local, Hash]
    val instructions = mutable.HashMap.empty[Hash, List[Inst.Let]]
    val localDefs    = mutable.HashMap.empty[Local, Inst]

    val hash       = new HashFunction(variableVN)
    val deepEquals = new DeepEquals(localDefs)

    def blockDominatedByDef(dominatedBlock: Block,
                            dominatingDef: Local): Boolean = {

      domination(dominatedBlock).exists { dominatingBlock =>
        val foundInParam = dominatingBlock.params.exists {
          case Val.Local(paramName, _) => (paramName == dominatingDef)
        }
        val foundInInsts = dominatingBlock.insts.exists {
          case Inst.Let(name, _) => (name == dominatingDef)
          case _                 => false
        }

        foundInParam || foundInInsts
      }
    }

    val newInsts = cfg.map { block =>
      variableVN ++= block.params.map(lval =>
        (lval.name, HashFunction.rawLocal(lval.name)))
      localDefs ++= block.params.map(lval => (lval.name, block.label))

      val newBlockInsts = block.insts.map {

        case inst: Inst.Let => {
          val idempotent = isIdempotent(inst.op)

          val instHash =
            if (idempotent)
              hash(inst.op)
            else
              inst.hashCode // hash the assigned variable as well, so a = op(b) and c = op(b) don't have the same hash

          variableVN += (inst.name -> instHash)
          localDefs += (inst.name  -> inst)

          if (idempotent) {
            val hashEqualInstrs = instructions.getOrElse(instHash, Nil)
            instructions += (instHash -> (inst :: hashEqualInstrs))

            val equalInstrs =
              hashEqualInstrs.filter(otherInst =>
                deepEquals.eqInst(inst, otherInst))
            val redundantInstrs = equalInstrs.filter(eqInst =>
              blockDominatedByDef(block, eqInst.name)) // only redundant if the current block is dominated by the block in which the equal instruction occurs

            val newInstOpt = redundantInstrs.headOption.map(
              redInst =>
                Inst.Let(inst.name,
                         Op.Copy(Val.Local(redInst.name, redInst.op.resty))))
            newInstOpt.getOrElse(inst)
          } else {
            inst
          }
        }

        case otherInst @ _ =>
          otherInst
      }

      block.label +: newBlockInsts
    }

    newInsts.flatten
  }

}

object GlobalValueNumbering extends PassCompanion {
  def isIdempotent(op: Op): Boolean = {
    import Op._
    op match {
      // Always idempotent:
      case (_: Pure | _: Method | _: Dynmethod | _: As | _: Is | _: Copy |
          _: Sizeof | _: Module | _: Field | _: Box | _: Unbox) =>
        true

      // Never idempotent:
      case (_: Load | _: Store | _: Stackalloc | _: Classalloc | _: Call |
          _: Closure) =>
        false
    }
  }

  class DeepEquals(localDefs: Local => Inst) {

    def eqInst(instA: Inst.Let, instB: Inst.Let): Boolean = {
      (instA.name == instB.name) || eqOp(instA.op, instB.op)
    }

    def eqOp(opA: Op, opB: Op): Boolean = {
      import Op._
      if (!(isIdempotent(opA) && isIdempotent(opB)))
        false
      else {
        (opA, opB) match {

          case (Elem(tyA, ptrA, indexesA), Elem(tyB, ptrB, indexesB)) =>
            eqType(tyA, tyB) && eqVal(ptrA, ptrB) && eqVals(indexesA, indexesB)

          case (Extract(aggrA, indexesA), Extract(aggrB, indexesB)) =>
            eqVal(aggrA, aggrB) && (indexesA == indexesB)

          case (Insert(aggrA, valueA, indexesA),
                Insert(aggrB, valueB, indexesB)) =>
            eqVal(aggrA, aggrB) && eqVal(valueA, valueB) && (indexesA == indexesB)

          // TODO handle commutativity of some bins
          case (Bin(binA, tyA, lA, rA), Bin(binB, tyB, lB, rB)) =>
            eqBin(binA, binB) && eqType(tyA, tyB) && eqVal(lA, lB) && eqVal(rA,
                                                                            rB)

          case (Comp(compA, tyA, lA, rA), Comp(compB, tyB, lB, rB)) =>
            eqComp(compA, compB) && eqType(tyA, tyB) && eqVal(lA, lB) && eqVal(
              rA,
              rB)

          case (Conv(convA, tyA, valueA), Conv(convB, tyB, valueB)) =>
            eqConv(convA, convB) && eqType(tyA, tyB) && eqVal(valueA, valueB)

          case (Select(condA, thenvA, elsevA),
                Select(condB, thenvB, elsevB)) =>
            eqVals(Seq(condA, thenvA, elsevA), Seq(condB, thenvB, elsevB))

          case (Field(objA, nameA), Field(objB, nameB)) =>
            eqVal(objA, objB) && eqGlobal(nameA, nameB)

          case (Method(objA, nameA), Method(objB, nameB)) =>
            eqVal(objA, objB) && eqGlobal(nameA, nameB)

          case (Dynmethod(objA, signatureA), Dynmethod(objB, signatureB)) =>
            eqVal(objA, objB) && signatureA == signatureB

          case (Module(nameA, _), Module(nameB, _)) =>
            eqGlobal(nameA, nameB)

          case (As(tyA, objA), As(tyB, objB)) =>
            eqType(tyA, tyB) && eqVal(objA, objB)

          case (Is(tyA, objA), Is(tyB, objB)) =>
            eqType(tyA, tyB) && eqVal(objA, objB)

          case (Copy(valueA), Copy(valueB)) =>
            eqVal(valueA, valueB)

          case (Sizeof(tyA), Sizeof(tyB)) =>
            eqType(tyA, tyB)

          case (Box(tyA, objA), Box(tyB, objB)) =>
            tyA == tyB && eqVal(objA, objB)

          case (Unbox(tyA, objA), Unbox(tyB, objB)) =>
            tyA == tyB && eqVal(objA, objB)

          case _ => false // non-matching pairs of ops, or not idempotent ones
        }
      }
    }

    def eqVal(valueA: Val, valueB: Val): Boolean = {
      import Val._
      (valueA, valueB) match {
        case (Struct(nameA, valuesA), Struct(nameB, valuesB)) =>
          eqGlobal(nameA, nameB) && eqVals(valuesA, valuesB)

        case (Array(elemtyA, valuesA), Array(elemtyB, valuesB)) =>
          eqType(elemtyA, elemtyB) && eqVals(valuesA, valuesB)

        case (Const(valueA), Const(valueB)) =>
          eqVal(valueA, valueB)

        case (Local(nameA, valtyA), Local(nameB, valtyB)) =>
          lazy val eqDefs = (localDefs(nameA), localDefs(nameB)) match {
            case (_: Inst.Label, _: Inst.Label)     => (nameA == nameB)
            case (instA: Inst.Let, instB: Inst.Let) => eqInst(instA, instB)
            case _                                  => false
          }
          eqType(valtyA, valtyB) && ((nameA == nameB) || eqDefs)

        case _ =>
          valueA == valueB
      }
    }

    def eqVals(valsA: Seq[Val], valsB: Seq[Val]): Boolean = {
      val sizeEqual = (valsA.size == valsB.size)
      lazy val contentEqual =
        valsA.zip(valsB).forall { case (a, b) => eqVal(a, b) }
      sizeEqual && contentEqual
    }

    def eqType(tyA: Type, tyB: Type): Boolean = {
      tyA == tyB
    }

    def eqGlobal(globalA: Global, globalB: Global): Boolean = {
      globalA == globalB
    }

    def eqBin(binA: Bin, binB: Bin): Boolean = {
      binA == binB
    }

    def eqComp(compA: Comp, compB: Comp): Boolean = {
      compA == compB
    }

    def eqConv(convA: Conv, convB: Conv): Boolean = {
      convA == convB
    }

  }

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
        case Call(ty, ptr, args, _) => "Call" +: ty +: ptr +: args
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

        case Field(obj, name)           => Seq("Field", obj, name)
        case Method(obj, name)          => Seq("Method", obj, name)
        case Dynmethod(obj, signature)  => Seq("Dynmethod", obj, signature)
        case As(ty, obj)                => Seq("As", ty, obj)
        case Is(ty, obj)                => Seq("Is", ty, obj)
        case Copy(value)                => Seq("Copy", value)
        case Closure(ty, fun, captures) => "Closure" +: ty +: fun +: captures

        case Classalloc(name) => Seq("Classalloc", name)
        case Module(name, _)  => Seq("Module", name)
        case Sizeof(ty)       => Seq("Sizeof", ty)
        case Box(code, obj)   => Seq("Box", code.toString, obj)
        case Unbox(code, obj) => Seq("Unbox", code.toString, obj)
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

  override def apply(config: tools.Config, top: Top) =
    new GlobalValueNumbering()
}
