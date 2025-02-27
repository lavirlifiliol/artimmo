package artimmo.model.actions

import artimmo.model
import artimmo.model.actions.internals.{Action, ReqEncoder, Response}
import zio.json._

object Gather {
  case class Drop(code: String, @jsonField("quantity") quant: Long)

  given JsonDecoder[Drop] = DeriveJsonDecoder.gen

  case class SkillInfo(xp: Long, items: Seq[Drop])

  given JsonDecoder[SkillInfo] = DeriveJsonDecoder.gen

  case class Resp(cooldown: model.Cooldown, details: SkillInfo, character: model.Character) extends Response
}

given gatherAction: Action[Gather.type, Gather.Resp] with {
  override def endpoint(character: String): String = s"/my/$character/action/gathering"

  private object Enc extends ReqEncoder[Gather.type] {
    override def toBody(t: Gather.type): CharSequence = ""
  }

  override def encoder: ReqEncoder[Gather.type] = Enc

  override def decoder: JsonDecoder[Gather.Resp] = DeriveJsonDecoder.gen
}