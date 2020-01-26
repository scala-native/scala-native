package scala.scalanative.cpp

import scala.scalanative.cpp._

class CppObject(private var nativeObject: NativeObject) {
    def this() = this(NullObj)

    def setNativeObject(obj: NativeObject): Unit = {
        nativeObject = obj
    }
    def getNativeObject(): NativeObject = nativeObject
    def destroy(): Unit  = {

    }
    def close() {
        
    }
}