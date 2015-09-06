package salty.tools.linker

import salty.util.sh

abstract class GlobalPass {
  def on(param: Param): Unit =
    on(param.ty)

  def on(ty: Type): Unit = ty match {
    case Type.Ref(ty)   => on(ty)
    case Type.Slice(ty) => on(ty)
    case Type.Of(defn)  => on(defn)
    case _              => ()
  }

  def on(meta: Meta): Unit = meta match {
    case Meta.Overrides(defn) => on(defn)
    case Meta.Belongs(defn) => on(defn)
  }

  def on(defn: Defn): Unit = defn match {
    case Class(parent, ifaces, meta) =>
      on(parent); ifaces.foreach(on); meta.foreach(on)
    case Interface(ifaces, meta) =>
      ifaces.foreach(on); meta.foreach(on)
    case Module(parent, ifaces, meta) =>
      on(parent); ifaces.foreach(on); meta.foreach(on)
    case Declare(ty, params, meta) =>
      on(ty); params.foreach(on); meta.foreach(on)
    case Define(ty, params, body, meta) =>
      on(ty); params.foreach(on); on(body); meta.foreach(on)
    case Field(ty, meta) =>
      on(ty); meta.foreach(on)
    case Extern(name, meta) =>
      meta.foreach(on)
  }

  def on(block: Block): Unit = {
    instrs.foreach(on); onTermn(block.termn)
  }

  def on(instr: Instr): Unit = instr match {
    case Instr.Assign(_, expr) => on(expr)
    case expr: Expr            => on(expr)
  }

  def on(expr: Expr): Unit = expr match {
    case Expr.Conv(_, _, ty) => on(ty)
    case Expr.Is(_, ty)      => on(ty)
    case Expr.Alloc(ty, _)   => on(ty)
    case Expr.Call(defn, _)  => on(defn)
    case Expr.Phi(branches)  => branches.foreach(on(br.block))
    case Expr.Box(_, ty)     => on(ty)
    case Expr.Unbox(_, ty)   => on(ty)
    case Val.Class(ty)       => on(ty)
    case Val.Of(defn)        => on(defn)
    case _                   => ()
  }
}

class ResolveAllExtern(scope: LinkerScope) extends GlobalPass
