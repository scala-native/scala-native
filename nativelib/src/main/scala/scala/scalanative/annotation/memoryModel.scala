package scala.scalanative
package annotation

import scala.annotation.meta.field

/** Follow the Java Memory Model and its final fields semantics when
 *  initializing and reading final fields.
 *
 *  The compiler would ensure that final field would be reachable in fully
 *  innitialized state by other reads, by introducing synchronization primitives
 *  on each it's access. Applies only to type immutable field mebers (`val`s)
 *
 *  Can be used either on single field or whole type if all of it's fields
 *  should be safetly published.
 */
@field
final class safePublish extends scala.annotation.StaticAnnotation
