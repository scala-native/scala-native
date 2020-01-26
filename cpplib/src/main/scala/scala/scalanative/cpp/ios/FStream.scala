package scala.scalanative.cpp.ios
import scalanative.unsafe._

class FStream(obj: NativeObject) extends IOStream(obj) {

    def is_open() : Boolean = {
        FStreamNative.is_open(getNativeObject())
    }
}

object FStream {
    
    def open(filepath: String, mask: Bitmask) : FStream = {
        Zone { implicit z =>
            new FStream(FStreamNative.open(toCString(filepath), mask))
        }
    }
}

@extern
object FStreamNative {
    @name("scalanative_cpp_ios_fstream_is_open")
    def is_open(obj: NativeObject): Boolean = extern

    @name("scalanative_cpp_ios_fstream_open")
    def open(filename: CString, mask: Bitmask): NativeObject = extern
}