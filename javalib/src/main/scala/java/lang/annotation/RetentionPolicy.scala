package java.lang.annotation

final class RetentionPolicy private (name: String, ordinal: Int)
    extends java.lang._Enum[RetentionPolicy](name, ordinal)

object RetentionPolicy {
  final val SOURCE = new RetentionPolicy("SOURCE", 0)
  final val CLASS = new RetentionPolicy("CLASS", 1)
  final val RUNTIME = new RetentionPolicy("RUNTIME", 2)

  def valueOf(name: String): RetentionPolicy =
    values().find(_.name() == name).getOrElse {
      throw new IllegalArgumentException(
        s"No enum constant java.lang.annotation.RetentionPolicy.$name"
      )
    }

  def values(): Array[RetentionPolicy] =
    Array(SOURCE, CLASS, RUNTIME)
}
