// format: off

package scala.scalanative.runtime;

import scala.scalanative.runtime.NativeVarHandle.FieldBinding;

/** JVM-only symbols for calls emitted by the compiler plugin.
 *
 * nativelib is compiled before javalib, where the Scala object with the real
 * implementation lives. The plugin's JVM tests only see nativelib. These
 * factories let those tests type the generated call; Native links it to the
 * Scala object instead. Access modes dispatch through NativeVarHandle.
 */
public final class VarHandle {
  private VarHandle() {}

  public static java.lang.invoke.VarHandle createBooleanHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createByteHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createShortHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createCharHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createIntHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createLongHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createFloatHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createDoubleHandle(FieldBinding binding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createReferenceHandle(FieldBinding binding, Class<?> variableType) { throw new AssertionError("stub"); }
}
