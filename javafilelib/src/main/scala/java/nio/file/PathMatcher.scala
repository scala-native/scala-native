package java.nio.file

trait PathMatcher {
  def matches(path: Path): Boolean
}
