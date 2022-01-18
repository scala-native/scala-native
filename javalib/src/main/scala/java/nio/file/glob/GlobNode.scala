package java.nio.file.glob

case class GlobNode( // add divs and chars left
    globSpec: GlobSpecification,
    next: List[GlobNode],
    isFinal: Boolean
)
