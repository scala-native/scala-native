package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.windows.HandleApi.Handle

@link("Advapi32")
@extern()
object SecurityBaseApi {
  // Internal Windows structures, might have variable size and should not be modifed by the user
  type SIDPtr = Ptr[Byte]
  type ACLPtr = Ptr[Byte]
}
