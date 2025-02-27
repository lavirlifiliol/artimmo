package artimmo.model.actions.common

import artimmo.model
import zio.json.{JsonDecoder, JsonEncoder}

trait Action[To, From <: Response] {
  def endpoint(character: String): String

  def encoder: ReqEncoder[To]

  def decoder: JsonDecoder[From]
}

trait ReqEncoder[T] {
  def toBody(t: T): CharSequence
}

class JsonReq[T](private val e: JsonEncoder[T]) extends ReqEncoder[T] {

  override def toBody(t: T): CharSequence = e.encodeJson(t)
}

trait Response {
  // todo is this needed
  // not sure how to make Resp[Move.Resp] have .destination and Resp[Fight.Resp] have .fight
  def cooldown: model.Cooldown

  def character: model.Character
}