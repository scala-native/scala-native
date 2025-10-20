object CanonicalPathTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(canon0F.getCanonicalPath == canon0N)
    assert(canon1F.getCanonicalPath == canon1N)
    assert(canon2F.getCanonicalPath == canon2N)
    assert(canon3F.getCanonicalPath == canon3N)
    assert(canon4F.getCanonicalPath == canon4N)
    assert(canon5F.getCanonicalPath == canon5N)
  }
}
