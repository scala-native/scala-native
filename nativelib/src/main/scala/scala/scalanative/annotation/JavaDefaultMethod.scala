// Ported from Scala.js commit SHA1: 9dc4d5b dated: 2020-10-18

package scala.scalanative
package annotation

/** Mark a concrete trait method as a Java default method.
 *
 *  This annotation can be used on concrete trait methods to mark them as
 *  Java default methods. This should be used *only* to implement interfaces
 *  of the JDK that have default methods in Java.
 *
 *  Otherwise using this annotation is unspecified.
 */
class JavaDefaultMethod extends scala.annotation.StaticAnnotation
