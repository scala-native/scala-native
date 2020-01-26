package scala.scalanative.cpp.ios
import scalanative.unsafe._

object OpenMode {
    val app: Bitmask = OpenModeNative.app
    val binary: Bitmask = OpenModeNative.binary
    val in: Bitmask = OpenModeNative.in
    val out: Bitmask = OpenModeNative.out
    val trunc: Bitmask = OpenModeNative.trunc
    val ate: Bitmask = OpenModeNative.ate
}

@extern
object OpenModeNative
{
    @name("scalanative_cpp_ios_openmode_app")
    def app: Bitmask = extern

    @name("scalanative_cpp_ios_openmode_binary")
    def binary: Bitmask = extern

    @name("scalanative_cpp_ios_openmode_in")
    def in: Bitmask = extern

    @name("scalanative_cpp_ios_openmode_out")
    def out: Bitmask = extern

    @name("scalanative_cpp_ios_openmode_trunc")
    def trunc: Bitmask = extern

    @name("scalanative_cpp_ios_openmode_ate")
    def ate: Bitmask = extern
}