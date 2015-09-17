package salty.ir

import salty.util.Serialize, Serialize.{Sequence => s}

object Serializers {
  implicit val serializeType: Serialize[Type] = Serialize { ??? }
  implicit val serializeInstr: Serialize[Instr] = Serialize { ??? }
  implicit val serializeDefn: Serialize[Defn] = Serialize { ??? }
  implicit val serializeRel: Serialize[Rel] = Serialize { ??? }
  implicit val serializeScope: Serialize[Scope] = Serialize { ??? }
}
