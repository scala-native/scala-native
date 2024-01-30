package scala.scalanative.build

/** An object describing configuration of the Scala Native semantics. */
sealed trait SemanticsConfig {
//format: off
  /** Describes Behaviour of final fields and their complaince with the Java Memory Model. 
   *  The outputs of the program would depend of compliance level:
   *  - [[JVMMemoryModelCompliance.Strict]] all final fields are synchronized - ensures safe publication,but it might lead to runtime performance overhead.
   *  - [[JVMMemoryModelCompliance.None]] final fields are never synchronized - no runtime overhead when accessing final fields, but it might lead to unexpected state in highly concurrent programs.
   *  - [[JVMMemoryModelCompliance.Relaxed]] (default) only fields marked with scala.scalanative.annotation.safePublish are synchronized.
   */
  def finalFields: JVMMemoryModelCompliance
// format: on
  def withFinalFields(value: JVMMemoryModelCompliance): SemanticsConfig

  private[scalanative] def show(indent: String): String
}

object SemanticsConfig {
  val default: SemanticsConfig =
    Impl(finalFields = JVMMemoryModelCompliance.Relaxed)

  private[build] case class Impl(finalFields: JVMMemoryModelCompliance)
      extends SemanticsConfig {
    override def withFinalFields(
        value: JVMMemoryModelCompliance
    ): SemanticsConfig = copy(finalFields = value)

    override def toString: String = show(indent = " ")
    override private[scalanative] def show(indent: String): String = {
      s"""SemanticsConfig(
          |$indent- finalFields: ${finalFields}
          |$indent)""".stripMargin
    }
  }
}

sealed abstract class JVMMemoryModelCompliance {
  final def isStrict = this == JVMMemoryModelCompliance.Strict
  final def isRelaxed = this == JVMMemoryModelCompliance.Relaxed
  final def isNone = this == JVMMemoryModelCompliance.None
}
object JVMMemoryModelCompliance {

  /** Guide toolchain to ignore JVM memory model specification */
  case object None extends JVMMemoryModelCompliance

  /** Guide toolchain to use a relaxed JVM memory model specification, typically
   *  guided with source code annotations
   */
  case object Relaxed extends JVMMemoryModelCompliance

  /** Guide toolchain to strictly follow JVM memory model */
  case object Strict extends JVMMemoryModelCompliance

}
