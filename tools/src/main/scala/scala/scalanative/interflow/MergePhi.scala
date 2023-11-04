package scala.scalanative
package interflow

final case class MergePhi(
    param: nir.Val.Local,
    incoming: Seq[(nir.Local, nir.Val)]
)
