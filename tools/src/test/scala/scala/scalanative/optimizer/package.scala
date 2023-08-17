package scala.scalanative

import org.junit.Assert._

package object optimizer {
  import nir._

  def assertContainsAll[T](
      msg: String,
      expected: Iterable[T],
      actual: Iterable[T]
  ) = {
    val left = expected.toSeq
    val right = actual.toSeq
    val diff = left.diff(right)
    assertTrue(s"$msg - not found ${diff} in $right", diff.isEmpty)
  }

  def assertContains[T](msg: String, expected: T, actual: Iterable[T]) = {
    assertTrue(
      s"$msg - not found ${expected} in ${actual.toSeq}",
      actual.find(_ == expected).isDefined
    )
  }

  def assertDistinct(localNames: Iterable[LocalName]) = {
    val duplicated =
      localNames.groupBy(identity).filter(_._2.size > 1).map(_._1)
    assertTrue(s"Found duplicated names of ${duplicated}", duplicated.isEmpty)
  }

  def namedLets(defn: nir.Defn.Define): Map[Inst.Let, LocalName] =
    defn.insts.collect {
      case inst: Inst.Let if defn.debugInfo.localNames.contains(inst.id) =>
        inst -> defn.debugInfo.localNames(inst.id)
    }.toMap

  protected[optimizer] def findEntry(
      linked: Seq[Defn],
      entryName: String = "Test"
  ): Option[Defn.Define] = {
    object TestMain {
      val TestModule = Global.Top(s"$entryName$$")
      val CompanionMain =
        TestModule.member(Rt.ScalaMainSig.copy(scope = Sig.Scope.Public))

      def unapply(name: Global): Boolean = name match {
        case CompanionMain => true
        case Global.Member(TestModule, sig) =>
          sig.unmangled match {
            case Sig.Duplicate(of, _) => of == CompanionMain.sig
            case _                    => false
          }
        case _ => false
      }
    }
    object TestMainForwarder {
      val staticForwarder = Global.Top("Test").member(Rt.ScalaMainSig)
      def unapply(name: Global): Boolean = name == staticForwarder
    }
    val companionMethod = linked
      .collectFirst { case defn @ Defn.Define(_, TestMain(), _, _, _) => defn }
    def staticForwarder = linked
      .collectFirst {
        case defn @ Defn.Define(_, TestMainForwarder(), _, _, _) => defn
      }
    companionMethod
      .orElse(staticForwarder)
      .ensuring(_.isDefined, "Not found linked method")
  }
}
