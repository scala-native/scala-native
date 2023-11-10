package scala.scalanative
package linker

trait Extractor[T] {

  def unapply(
      ty: nir.Type
  )(implicit analysis: ReachabilityAnalysis.Result): Option[T] = ty match {
    case ty: nir.Type.RefKind =>
      unapply(ty.className)
    case _ =>
      None
  }
  def unapply(name: nir.Global)(implicit
      analysis: ReachabilityAnalysis.Result
  ): Option[T]

}

object Ref extends Extractor[Info] {
  def unapply(name: nir.Global)(implicit
      analysis: ReachabilityAnalysis.Result
  ): Option[Info] =
    analysis.infos.get(name)
}

object ScopeRef extends Extractor[ScopeInfo] {
  def unapply(
      name: nir.Global
  )(implicit analysis: ReachabilityAnalysis.Result): Option[ScopeInfo] =
    analysis.infos.get(name).collect {
      case node: ScopeInfo => node
    }
}

object ClassRef extends Extractor[Class] {
  def unapply(
      name: nir.Global
  )(implicit analysis: ReachabilityAnalysis.Result): Option[Class] =
    analysis.infos.get(name).collect {
      case node: Class => node
    }
}

object TraitRef extends Extractor[Trait] {
  def unapply(
      name: nir.Global
  )(implicit analysis: ReachabilityAnalysis.Result): Option[Trait] =
    analysis.infos.get(name).collect {
      case node: Trait => node
    }
}

object MethodRef extends Extractor[(Info, Method)] {
  def unapply(
      name: nir.Global
  )(implicit analysis: ReachabilityAnalysis.Result): Option[(Info, Method)] =
    analysis.infos.get(name).collect {
      case node: Method => (node.owner, node)
    }
}

object FieldRef extends Extractor[(Info, Field)] {
  def unapply(
      name: nir.Global
  )(implicit analysis: ReachabilityAnalysis.Result): Option[(Info, Field)] =
    analysis.infos.get(name).collect {
      case node: Field => (node.owner, node)
    }
}

object ArrayRef {
  def unapply(ty: nir.Type): Option[(nir.Type, Boolean)] = ty match {
    case nir.Type.Array(ty, nullable) =>
      Some((ty, nullable))
    case nir.Type.Ref(name, _, nullable) =>
      nir.Type.fromArrayClass(name).map(ty => (ty, nullable))
    case _ =>
      None
  }
}

object ExactClassRef {
  def unapply(
      ty: nir.Type
  )(implicit analysis: ReachabilityAnalysis.Result): Option[(Class, Boolean)] =
    ty match {
      case nir.Type.Ref(ClassRef(cls), exact, nullable)
          if exact || cls.subclasses.isEmpty =>
        Some((cls, nullable))
      case UnitRef(nullable) =>
        Some(
          (analysis.infos(nir.Rt.BoxedUnit.name).asInstanceOf[Class], nullable)
        )
      case nir.Type.Array(ty, nullable) =>
        Some(
          (
            analysis.infos(nir.Type.toArrayClass(ty)).asInstanceOf[Class],
            nullable
          )
        )
      case _ =>
        None
    }
}

object UnitRef {
  def unapply(ty: nir.Type): Option[Boolean] = ty match {
    case nir.Type.Unit =>
      Some(false)
    case nir.Type.Ref(name, _, nullable)
        if name == nir.Rt.BoxedUnit.name
          || name == nir.Rt.BoxedUnitModule.name =>
      Some(nullable)
    case _ =>
      None
  }
}

object BoxRef {
  def unapply(ty: nir.Type): Option[(nir.Type, Boolean)] = ty match {
    case nir.Type.Ref(name, _, nullable) =>
      nir.Type.unbox.get(nir.Type.Ref(name)).map(ty => (ty, nullable))
    case _ =>
      None
  }
}
