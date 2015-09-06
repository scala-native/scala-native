package salty.ir

import salty.ir.Combinators._
import salty.util.Serialize, Serialize.{Sequence => s}

object Serializers {
  implicit val serializeType: Serialize[Type] = Serialize { ??? }
  implicit val serializeNode: Serialize[Node] = Serialize { ??? }
  implicit val serializeOp: Serialize[Op] = Serialize { ??? }
  implicit val serializeDefn: Serialize[Defn] = Serialize { ??? }
  implicit val serializeMeta: Serialize[Meta] = Serialize { ??? }
  implicit val serializeScope: Serialize[Scope] = Serialize { ??? }
}
