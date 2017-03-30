package java.nio.file.attribute

trait FileAttributeView extends AttributeView {
  // Extended API:
  def getAttribute(name: String): Object =
    throw new IllegalArgumentException()
}
