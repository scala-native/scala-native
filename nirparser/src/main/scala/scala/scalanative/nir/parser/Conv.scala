package scala.scalanative
package nir
package parser

import fastparse._

object Conv extends Base[nir.Conv] {

  def Trunc[_: P]    = P("trunc".!.map(_ => nir.Conv.Trunc))
  def Zext[_: P]     = P("zext".!.map(_ => nir.Conv.Zext))
  def Sext[_: P]     = P("sext".!.map(_ => nir.Conv.Sext))
  def Fptrunc[_: P]  = P("fptrunc".!.map(_ => nir.Conv.Fptrunc))
  def Fpext[_: P]    = P("fpext".!.map(_ => nir.Conv.Fpext))
  def Fptoui[_: P]   = P("fptoui".!.map(_ => nir.Conv.Fptoui))
  def Fptosi[_: P]   = P("fptosi".!.map(_ => nir.Conv.Fptosi))
  def Uitofp[_: P]   = P("uitofp".!.map(_ => nir.Conv.Uitofp))
  def Sitofp[_: P]   = P("sitofp".!.map(_ => nir.Conv.Sitofp))
  def Ptrtoint[_: P] = P("ptrtoint".!.map(_ => nir.Conv.Ptrtoint))
  def Inttoptr[_: P] = P("inttoptr".!.map(_ => nir.Conv.Inttoptr))
  def Bitcast[_: P]  = P("bitcast".!.map(_ => nir.Conv.Bitcast))
  override def parser[_: P]: P[nir.Conv] =
    Trunc | Zext | Sext | Fptrunc | Fpext | Fptoui | Fptosi | Uitofp | Sitofp | Ptrtoint | Inttoptr | Bitcast
}
