package java.lang.invoke

import scala.scalanative.annotation.stub

// scalafmt: { maxColumn = 120}

/** Compile-time marker for field VarHandle lookup.
 *
 *  Scala Native deliberately does not provide reflective lookup at runtime. The compiler plugin recognises literal
 *  findVarHandle/findStaticVarHandle calls and either lowers them to a direct field operation or reports a compile-time
 *  error.
 */
object MethodHandles {
  @stub() def lookup(): Lookup = ???
  @stub() def privateLookupIn(targetClass: Class[_], caller: Lookup): Lookup = ???

  final class Lookup private[invoke] () {
    @stub() def findVarHandle(refc: Class[_], name: String, `type`: Class[_]): VarHandle = ???
    @stub() def findStaticVarHandle(refc: Class[_], name: String, `type`: Class[_]): VarHandle = ???
  }
}
