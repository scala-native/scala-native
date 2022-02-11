package java.nio.file.glob

private[glob] sealed abstract class GlobNode(
    val minCharsLeft: Int,
    val minDivsLeft: Int
)
private[glob] case class StartNode(nextNode: GlobNode) extends GlobNode(0, 0)
private[glob] case object EndNode extends GlobNode(0, 0)
private[glob] case class TransitionNode(
    globSpec: GlobSpecification,
    next: List[GlobNode],
    override val minCharsLeft: Int,
    override val minDivsLeft: Int
) extends GlobNode(minCharsLeft, minDivsLeft)
