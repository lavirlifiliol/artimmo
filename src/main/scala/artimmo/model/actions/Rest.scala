package artimmo.model.actions

import artimmo.model
import artimmo.model.actions.common.{Action, ReqEncoder, Response}
import zio.json._

object Rest {
  case class Resp(cooldown: model.Cooldown, @jsonField("hp_restored") hpRestored: Long, character: model.Character)
      extends Response
}

given restAction: Action[Rest.type, Rest.Resp] with {
  override def endpoint(character: String): String = s"/my/$character/action/rest"

  private object Enc extends ReqEncoder[Rest.type] {
    override def toBody(t: Rest.type): CharSequence = ""
  }

  override def encoder: ReqEncoder[Rest.type] = Enc

  override def decoder: JsonDecoder[Rest.Resp] = DeriveJsonDecoder.gen
}
