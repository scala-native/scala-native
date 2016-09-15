import scalanative.native._
import scalanative.native.stdio._
import scalanative.native.string._

object String2CString {
 def main (args: Array[String]) {
   val cstrFrom = c"1234"

   fprintf(stdout, c"from: |%s|\n", cstrFrom)

   val szTo = fromCString(cstrFrom)

   assert(szTo.size == 4)
   assert(szTo.charAt(0) == '1')
   assert(szTo.charAt(1) == '2')
   assert(szTo.charAt(2) == '3')
   assert(szTo.charAt(3) == '4')

   fprintf(stdout, c"from OK\n")

   val szFrom = "abcde"
   val cstrTo = toCString(szFrom)

   fprintf(stdout, c"to: |%s|\n", cstrTo)

   assert(strlen(cstrTo) == 5)
   assert(cstrTo(0) == 'a'.toByte)
   assert(cstrTo(1) == 'b'.toByte)
   assert(cstrTo(2) == 'c'.toByte)
   assert(cstrTo(3) == 'd'.toByte)
   assert(cstrTo(4) == 'e'.toByte)

   fprintf(stdout, c"to OK\n")
 }
}
