object GetParentTest {
  import Files._
  import scala.scalanative.runtime.Platform
  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(children0.getParent == expectedParent0)
    assert(children1.getParent == expectedParent1)
    assert(children2.getParent == expectedParent2)
    assert(children3.getParent == expectedParent3)
    assert(children4.getParent == expectedParent4)
    assert(children5.getParentFile == expectedParent5)
  }
}
