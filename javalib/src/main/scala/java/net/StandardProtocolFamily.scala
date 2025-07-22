package java.net

sealed class StandardProtocolFamily(name: String, ordinal: Int)
    extends _Enum[StandardProtocolFamily](name, ordinal)
    with ProtocolFamily {
  override def toString() = this.name
}

object StandardProtocolFamily {
  val INET = new StandardProtocolFamily("INET", 0)
  val INET6 = new StandardProtocolFamily("INET6", 1)

  private val cachedValues = Array(INET, INET6)
  def values(): Array[StandardProtocolFamily] = cachedValues.clone()
  def valueOf(name: String): StandardProtocolFamily = {
    cachedValues.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum const StandardProtocolFamily." + name
      )
    }
  }
}
