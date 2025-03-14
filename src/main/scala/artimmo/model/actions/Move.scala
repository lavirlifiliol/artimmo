package artimmo.model.actions

import artimmo.*
import artimmo.model.actions.common.{Action, JsonReq, ReqEncoder, Response}
import zio.json.*

case class Move(x: Long, y: Long)

object Move {
  case class Content(@jsonField("type") kind: String, code: String) // TODO type safety

  given JsonDecoder[Content] = DeriveJsonDecoder.gen[Content]

  case class Map(
      name: String,
      skin: String,
      x: Long,
      y: Long,
      content: Option[Content]
  )

  given JsonDecoder[Map] = DeriveJsonDecoder.gen[Map]

  case class Resp(cooldown: model.Cooldown, destination: Map, character: model.Character) extends Response
}

given moveAction: Action[Move, Move.Resp] with {
  override def endpoint(character: String): String = s"/my/$character/action/move"

  override def encoder: ReqEncoder[Move] = JsonReq(DeriveJsonEncoder.gen)

  override def decoder: JsonDecoder[Move.Resp] = DeriveJsonDecoder.gen
}
