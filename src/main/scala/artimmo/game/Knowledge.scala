package artimmo.game

import artimmo.model
import artimmo.model.Code

case class Knowledge(fixed: Knowledge.Fixed, chars: Map[String, model.Character])

object Knowledge {
  case class Fixed(
      resources: Map[Code.Resource, model.Resource],
      items: Map[Code.Item, model.Item]
  )
}
