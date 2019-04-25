package scala.scalanative
package linker

import nir._

trait Result {

  /** Sequence of globals that could not be resolved. */
  private[scalanative] def unresolved: Seq[Global]

  /** Map that stores the dependencies between globals. */
  private[scalanative] def referencedFrom: Map[Global, Global]

  /** Sequence of external c libraries to link with. */
  private[scalanative] def links: Seq[Attr.Link]

  /** Sequence of definitions that were discovered during linking. */
  private[scalanative] def defns: Seq[nir.Defn]

  /** Sequence of signatures of dynamic methods that were discovered during linking. */
  private[scalanative] def dyns: Seq[String]

  /** Create a copy of the result with given unresolved sequence. */
  private[scalanative] def withUnresolved(value: Seq[Global]): Result

  /** Create a copy of the result with given referenced from map. */
  private[scalanative] def withReferencedFrom(
      value: Map[Global, Global]): Result

  /** Create a copy of the result with given links sequence. */
  private[scalanative] def withLinks(value: Seq[Attr.Link]): Result

  /** Create a copy of the result with given defns sequence. */
  private[scalanative] def withDefns(value: Seq[nir.Defn]): Result

  /** Create a copy of the result with given dyns sequence. */
  private[scalanative] def withDyns(value: Seq[String]): Result
}

object Result {

  /** Default, empty linker result. */
  val empty: Result =
    Impl(Seq.empty, Map.empty, Seq.empty, Seq.empty, Seq.empty)

  private[linker] final case class Impl(unresolved: Seq[Global],
                                        referencedFrom: Map[Global, Global],
                                        links: Seq[Attr.Link],
                                        defns: Seq[nir.Defn],
                                        dyns: Seq[String])
      extends Result {
    def withUnresolved(value: Seq[Global]): Result =
      copy(unresolved = value)

    def withReferencedFrom(value: Map[Global, Global]): Result =
      copy(referencedFrom = value)

    def withLinks(value: Seq[Attr.Link]): Result =
      copy(links = value)

    def withDefns(value: Seq[nir.Defn]): Result =
      copy(defns = value)

    def withDyns(value: Seq[String]): Result =
      copy(dyns = value)
  }

  private[linker] def apply(unresolved: Seq[Global],
                            from: Map[Global, Global],
                            links: Seq[Attr.Link],
                            defns: Seq[nir.Defn],
                            dyns: Seq[String]): Result =
    Impl(unresolved, from, links, defns, dyns)
}
