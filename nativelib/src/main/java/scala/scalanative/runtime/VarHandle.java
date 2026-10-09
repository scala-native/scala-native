// format: off

package scala.scalanative.runtime;

import scala.scalanative.runtime.NativeVarHandle.FieldBinding;

/** JVM-only symbols for calls emitted by the compiler plugin.
 *
 * nativelib is compiled before javalib, where the Scala object with the real
 * implementation lives. The plugin's JVM tests only see nativelib. These
 * factories let those tests type the generated call; Native links it to the
 * Scala object instead. Access modes dispatch through NativeVarHandle.
 * Return the JDK-independent protocol so these symbols can be loaded against
 * the Java 8 API without resolving the JDK VarHandle class.
 */
public final class VarHandle {
  private VarHandle() {}

  public static NativeVarHandle createBooleanHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createByteHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createShortHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createCharHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createIntHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createLongHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createFloatHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createDoubleHandle(FieldBinding binding, Class<?> coordinateType) { throw new AssertionError("stub"); }
  public static NativeVarHandle createReferenceHandle(FieldBinding binding, Class<?> coordinateType, Class<?> variableType) { throw new AssertionError("stub"); }
}
