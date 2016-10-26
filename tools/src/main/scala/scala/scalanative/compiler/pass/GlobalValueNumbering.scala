package scala.scalanative
package compiler
package pass

import analysis.ClassHierarchy.Top
import analysis.ClassHierarchyExtractors._
import nir._, Shows._

object GlobalValueNumbering {

  def isIdempotent(op: Op)(implicit top: Top): Boolean = {
    import Op._
    op match {
      // Always idempotent:
      case (_: Pure | _: Method | _: As | _: Is | _: Copy | _: Sizeof |
          _: Module | _: Field) =>
        true

      // Never idempotent:
      case (_: Load | _: Store | _: Stackalloc | _: Classalloc) => false

      // Special cases:
      case Call(_, Val.Global(Ref(node), _), _) =>
        node.attrs.isPure

      case Call(_, _, _) => false

      case _: Closure => ???
    }
  }

  class DeepEquals(localDefs: Local => Inst)(implicit top: Top) {

    def eqInst(instA: Inst.Let, instB: Inst.Let): Boolean = {
      (instA.name == instB.name) || eqOp(instA.op, instB.op)
    }

    def eqOp(opA: Op, opB: Op): Boolean = {
      import Op._
      if (!(isIdempotent(opA) && isIdempotent(opB)))
        false
      else {
        (opA, opB) match {
          // Here we know that the function called is idempotent
          case (Call(tyA, ptrA, argsA), Call(tyB, ptrB, argsB)) =>
            eqType(tyA, tyB) && eqVal(ptrA, ptrB) && eqVals(argsA, argsB)

          case (Load(_, _), Load(_, _)) =>
            false // non idempotent

          case (Store(_, _, _), Store(_, _, _)) =>
            false // non idempotent

          case (Elem(tyA, ptrA, indexesA), Elem(tyB, ptrB, indexesB)) =>
            eqType(tyA, tyB) && eqVal(ptrA, ptrB) && eqVals(indexesA, indexesB)

          case (Extract(aggrA, indexesA), Extract(aggrB, indexesB)) =>
            eqVal(aggrA, aggrB) && indexesA.zip(indexesB).forall {
              case (m, n) => m == n
            }

          case (Insert(aggrA, valueA, indexesA),
                Insert(aggrB, valueB, indexesB)) =>
            eqVal(aggrA, aggrB) && eqVal(valueA, valueB) && indexesA
              .zip(indexesB)
              .forall { case (m, n) => m == n }

          case (Stackalloc(_, _), Stackalloc(_, _)) =>
            false // non idempotent

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

          case (Classalloc(_), Classalloc(_)) =>
            false // non idempotent

          case (Field(tyA, objA, nameA), Field(tyB, objB, nameB)) =>
            eqType(tyA, tyB) && eqVal(objA, objB) && eqGlobal(nameA, nameB)

          case (Method(tyA, objA, nameA), Method(tyB, objB, nameB)) =>
            eqType(tyA, tyB) && eqVal(objA, objB) && eqGlobal(nameA, nameB)

          case (Module(nameA), Module(nameB)) =>
            eqGlobal(nameA, nameB)

          case (As(tyA, objA), As(tyB, objB)) =>
            eqType(tyA, tyB) && eqVal(objA, objB)

          case (Is(tyA, objA), Is(tyB, objB)) =>
            eqType(tyA, tyB) && eqVal(objA, objB)

          case (Copy(valueA), Copy(valueB)) =>
            eqVal(valueA, valueB)

          case (Sizeof(tyA), Sizeof(tyB)) =>
            eqType(tyA, tyB)

          case (Closure(tyA, funA, capturesA),
                Closure(tyB, funB, capturesB)) =>
            eqType(tyA, tyB) && eqVal(funA, funB) && eqVals(capturesA,
                                                            capturesB)

          case _ => false // non-matching pairs of ops
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
      valsA.zip(valsB).forall { case (a, b) => eqVal(a, b) }
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

}
