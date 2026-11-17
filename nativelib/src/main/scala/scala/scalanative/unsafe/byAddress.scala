package scala.scalanative
package unsafe

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter, setter}

/** Binds an extern variable of a pointer type to the address of the native
 *  symbol, instead of to the value stored in it.
 *
 *  Use it for C arrays of unknown size, which can't be bound as a `CArray`:
 *  {{{
 *  // extern const char version[];
 *  @extern object lib {
 *    @byAddress var version: CString = extern
 *  }
 *  }}}
 *
 *  Variables annotated with `@byAddress` cannot be assigned.
 */
@field
@getter
@setter
final class byAddress extends StaticAnnotation
