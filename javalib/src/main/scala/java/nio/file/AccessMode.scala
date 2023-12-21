package java.nio.file

sealed class AccessMode(name: String, ordinal: Int)
    extends _Enum[AccessMode](name, ordinal) {
  override def toString() = this.name
}

object AccessMode {
  final val EXECUTE = new AccessMode("EXECUTE", 0)
  final val READ = new AccessMode("READ", 1)
  final val WRITE = new AccessMode("WRITE", 1)

  private val cachedValues =
    Array(EXECUTE, READ, WRITE)
  def values(): Array[AccessMode] = cachedValues.clone()
  def valueOf(name: String): AccessMode = {
    cachedValues.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException("No enum const AccessMode." + name)
    }
  }
}
