package java.util.jar

// Ported from Apache Harmony

import java.util.{Collection, HashMap, Map, Set}

class Attributes private (protected var map: Map[Object, Object])
    extends Cloneable
    with Map[Object, Object] {
  def this() = this(new HashMap[Object, Object]())
  def this(size: Int) = this(new HashMap[Object, Object](size))
  def this(attributes: Attributes) =
    this(
      attributes.map
        .asInstanceOf[HashMap[?, ?]]
        .clone()
        .asInstanceOf[Map[Object, Object]]
    )

  override def clear(): Unit =
    map.clear()

  override def containsKey(key: Any): Boolean =
    map.containsKey(key)

  override def containsValue(value: Any): Boolean =
    map.containsValue(value)

  override def entrySet(): Set[Map.Entry[Object, Object]] =
    map.entrySet()

  override def get(key: Any): Object =
    map.get(key)

  override def isEmpty(): Boolean =
    map.isEmpty()

  override def keySet(): Set[Object] =
    map.keySet()

  override def put(key: Object, value: Object): Object =
    map.put(key.asInstanceOf[Attributes.Name], value.asInstanceOf[String])

  override def putAll(attrib: Map[? <: Object, ? <: Object]): Unit =
    if (attrib == null || !attrib.isInstanceOf[Attributes]) {
      throw new ClassCastException()
    } else {
      this.map.putAll(attrib)
    }

  override def remove(key: Any): Object =
    map.remove(key)

  override def size(): Int =
    map.size()

  override def values(): Collection[Object] =
    map.values()

  override def clone(): Object = {
    val clone =
      try super.clone().asInstanceOf[Attributes]
      catch { case _: CloneNotSupportedException => null }

    if (clone == null) null
    else {
      clone.map = (map
        .asInstanceOf[HashMap[?, ?]]
        .clone())
        .asInstanceOf[Map[Object, Object]]
      clone
    }
  }

  override def hashCode(): Int =
    map.hashCode()

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Attributes =>
        this.eq(other) || this.map.equals(other.map)
      case _ =>
        false
    }

  def getValue(name: Attributes.Name): String =
    map.get(name).asInstanceOf[String]

  def getValue(name: String): String =
    map.get(new Attributes.Name(name)).asInstanceOf[String]

  def putValue(name: String, v: String): String =
    map.put(new Attributes.Name(name), v).asInstanceOf[String]

  private[jar] def getMap(): Map[Object, Object] =
    map
}

object Attributes {
  class Name private[jar] (private val name: Array[Byte]) {
    def this(s: String) = this(Name.toByteArray(s))
    private var hc: Int = 0

    private[jar] def getBytes(): Array[Byte] =
      name

    override def toString(): String =
      new String(name, "ISO-8859-1")

    override def equals(obj: Any): Boolean =
      obj match {
        case other: Name =>
          JarFile.asciiEqualsIgnoreCase(
            new String(name),
            new String(other.name)
          )
        case _ => false
      }

    override def hashCode(): Int = {
      if (hc == 0) {
        var hash = 0
        var multiplier = 1
        var i = name.length - 1
        while (i >= 0) {
          // 'A' & 0xDF == 'a' & 0xDF, ..., 'Z' & 0xDF == 'z' & 0xDF
          hash += (name(i) & 0xdf) * multiplier
          val shifted = multiplier << 5
          multiplier = shifted - multiplier
          i -= 1
        }
        hc = hash
      }
      hc
    }
  }
  object Name {
    final val CLASS_PATH: Name = new Name("Class-Path")
    final val MANIFEST_VERSION: Name = new Name("Manifest-Version")
    final val MAIN_CLASS: Name = new Name("Main-Class")
    final val SIGNATURE_VERSION: Name = new Name("Signature-Version")
    final val CONTENT_TYPE: Name = new Name("Content-Type")
    final val SEALED: Name = new Name("Sealed")
    final val IMPLEMENTATION_TITLE: Name = new Name("Implementation-Title")
    final val IMPLEMENTATION_VERSION: Name = new Name("Implementation-Version")
    final val IMPLEMENTATION_VENDOR: Name = new Name("Implementation-Vendor")
    final val SPECIFICATION_TITLE: Name = new Name("Specification-Title")
    final val SPECIFICATION_VERSION: Name = new Name("Specification-Version")
    final val SPECIFICATION_VENDOR: Name = new Name("Specification-Vendor")
    final val EXTENSION_LIST: Name = new Name("Extension-List")
    final val EXTENSION_NAME: Name = new Name("Extension-Name")
    final val EXTENSION_INSTALLATION: Name = new Name("Extension-Installation")
    final val IMPLEMENTATION_VENDOR_ID: Name = new Name(
      "Implementation-Vendor-Id"
    )
    final val IMPLEMENTATION_URL: Name = new Name("Implementation-URL")
    private[jar] final val NAME: Name = new Name("Name")

    private def toByteArray(s: String): Array[Byte] = {
      var i = s.length()
      if (i == 0 || i > Manifest.LINE_LENGTH_LIMIT - 2) {
        throw new IllegalArgumentException()
      }
      val name = new Array[Byte](i)
      i -= 1
      while (i >= 0) {
        val ch = s.charAt(i)
        if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_' || ch == '-' || (ch >= '0' && ch <= '9'))) {
          throw new IllegalArgumentException()
        }
        name(i) = ch.toByte
        i -= 1
      }
      name
    }
  }
}
