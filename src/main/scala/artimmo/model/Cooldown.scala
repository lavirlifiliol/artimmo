package artimmo.model

import zio.json._

// todo
case class Cooldown()

object Cooldown {
  implicit val jsonDecoder: JsonDecoder[Cooldown] = DeriveJsonDecoder.gen
}
