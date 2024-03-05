// Classes defined in this file are registered inside Scala Native compiler plugin,
// compiling them in javalib would lead to fatal error of compiler. They need
// to be defined with a different name and renamed when generating NIR name

package java.lang

trait _Comparable[A] {
  def compareTo(o: A): scala.Int
}
