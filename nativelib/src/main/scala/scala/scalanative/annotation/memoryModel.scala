package scala.scalanative
package annotation

import scala.annotation.meta.field

/** Follow the Java Memory Model and its final fields semantics when
 *  initializing and reading final fields.
 *
 *  The compiler would ensure that final field would be reachable in fully
 *  initialized state by other reads, by introducing synchronization primitives
 *  on each access. Applies only to type immutable field members (`val`s)
 *
 *  Can be used either on single field or whole type if all of its fields should
 *  be safely published.
 */
@field
final class safePublish extends scala.annotation.StaticAnnotation
