package artimmo.model

import zio.json._

case class Effect(code: Code.Effect, value: Long)

given JsonDecoder[Effect] = DeriveJsonDecoder.gen
