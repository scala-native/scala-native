package scala.scalanative
package unsafe

/** An annotation that is used to automatically compile foreign code if
 *  annotated symbol would be used in linked application
 *  @param sourcePath
 *    relative path the C, C++, Assembly or other support source file. The path
 *    would be resolved from the `resources/scala-native` directory of current
 *    project.
 *  @param compileOptions
 *    list of optional compiler options which would be applied when compiling
 *    the source files
 */
final class compile(sourcePath: String, compileOptions: String*)
    extends scala.annotation.StaticAnnotation
