package java.nio.file.attribute

import java.util.{HashMap, Map}

trait FileAttributeView extends AttributeView {
  // Extended API:
  def asMap: Map[String, Object] =
    new HashMap[String, Object]()
  def getAttribute(name: String): Object =
    asMap.get(name)
  def setAttribute(name: String, value: Object): Unit =
    throw new IllegalArgumentException()
}
