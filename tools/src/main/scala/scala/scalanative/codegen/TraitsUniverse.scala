package scala.scalanative
package codegen

import linker.Trait
import scala.collection.mutable

private[codegen] object TraitsUniverse {
  class TraitId private (val value: Int) extends AnyVal {
    def itablePosition(itableSize: Int): Int = value & (itableSize - 1)
  }
  object TraitId {
    // We use 1 bit to signal that id is a trait
    final val TraitMarker = 1 << 31

    class Context(maxColor: Int) {
      // Maximal number of bits required to represent all colors
      // Required for unique id assignment
      final val ColorBits =
        math.ceil(math.log(maxColor + 1) / math.log(2)).toInt
      final val MaxColor = (1 << ColorBits) - 1
      final val MaxTraitIdx = (~(TraitMarker | MaxColor)) >> ColorBits
    }
    def assign(color: Int, index: Int)(implicit ctx: Context): TraitId = {
      val id = (index << ctx.ColorBits) | color | TraitMarker
      new TraitId(id)
    }
    def unsafe(value: Int): TraitId = new TraitId(value)
  }
}

private[codegen] class TraitsUniverse(traits: Seq[Trait]) {
  import TraitsUniverse.*

  private val adjacencyList = mutable.Map.empty[Trait, mutable.Set[Trait]]

  // Init graph of traits relativity
  // Two traits share the edge if there exists a common class implementing both of them
  locally {
    val implementorMap = mutable.Map.empty[linker.Class, mutable.Set[Trait]]
    for {
      traitInstance <- traits
      impl <- traitInstance.implementors
    } {
      val relatedTraits =
        implementorMap.getOrElseUpdate(impl, mutable.Set.empty)
      for (relatedTrait <- relatedTraits) {
        this.addEdge(traitInstance, relatedTrait)
      }
      relatedTraits += traitInstance
    }
  }

  // Graph coloring
  // Two traits can have the same color only if there is no class that implement both of them
  val (colors: Map[Trait, Int], maxColor: Int) = {
    val colors = mutable.Map.empty[Trait, Int]
    colors.sizeHint(traits)
    val minColor = 0
    var maxColor = minColor
    for (cls <- traits) {
      val neighborColors = this.neighbors(cls).flatMap(colors.get)
      colors(cls) = minColor
        .to(maxColor)
        .find(color => !neighborColors.contains(color))
        .getOrElse {
          maxColor += 1
          maxColor
        }
    }
    (colors.toMap, maxColor)
  }

  implicit val idsContext: TraitId.Context = new TraitId.Context(maxColor)

  // Assign unique id for each trait using the
  def assignIds(ids: mutable.Map[linker.ScopeInfo, Int]): TraitId.Context = {
    // Each class within assigned group has assignd unique id
    val colorIndexer = mutable.Map.empty[Int, Iterator[Int]]
    for (cls <- traits) {
      val color = colors(cls)
      val indexer = colorIndexer.getOrElseUpdate(color, Iterator.from(0))
      val idx = indexer.next()
      assert(
        idx < idsContext.MaxTraitIdx,
        s"Exceeds maximal ammount of traits, MaxTraitIdx=${idsContext.MaxTraitIdx}, ColorBits=${idsContext.ColorBits}"
      )
      ids(cls) = TraitId.assign(color, idx).value
    }
    idsContext
  }

  private def addVertex(vertex: Trait): Unit =
    adjacencyList.getOrElseUpdate(vertex, mutable.Set.empty)

  private def addEdge(vertex1: Trait, vertex2: Trait): Unit = {
    addVertex(vertex1)
    addVertex(vertex2)
    adjacencyList(vertex1) += vertex2
    adjacencyList(vertex2) += vertex1
  }

  private def vertices: Iterable[Trait] = adjacencyList.keys
  private def neighbors(vertex: Trait): Set[Trait] =
    adjacencyList.getOrElse(vertex, Set.empty).toSet
}
