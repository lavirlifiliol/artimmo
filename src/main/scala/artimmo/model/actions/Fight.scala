package artimmo.model.actions

import artimmo.model
import artimmo.model.actions.common.{Action, ReqEncoder, Response}
import zio.json._

object Fight {
  case class Drop(code: String, @jsonField("quantity") quant: Long)

  given JsonDecoder[Drop] = DeriveJsonDecoder.gen

  // TODO blocked hits
  case class Fight(xp: Long, gold: Long, drops: Seq[Drop], turns: Long, logs: Seq[String], result: "win" | "loss")

  given JsonDecoder[Fight] = DeriveJsonDecoder.gen

  case class Resp(cooldown: model.Cooldown, fight: Fight, character: model.Character) extends Response
}

given fightAction: Action[Fight.type, Fight.Resp] with {
  override def endpoint(character: String): String = s"/my/$character/action/fight"

  private object Enc extends ReqEncoder[Fight.type] {
    override def toBody(t: Fight.type): CharSequence = ""
  }

  override def encoder: ReqEncoder[Fight.type] = Enc

  override def decoder: JsonDecoder[Fight.Resp] = DeriveJsonDecoder.gen
}
