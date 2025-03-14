package artimmo.model

import zio.json.JsonDecoder

object Code {
  case class Resource(code: String)

  given JsonDecoder[Resource] = JsonDecoder[String].map(Resource.apply)

  case class Skill(code: String) {
    def level(skills: Skills): Option[Level] =
      code match
        case "woodcutting"     => Some(skills.woodcutting)
        case "fishing"         => Some(skills.fishing)
        case "weaponcrafting"  => Some(skills.weaponcrafting)
        case "gearcrafting"    => Some(skills.gearcrafting)
        case "jewelrycrafting" => Some(skills.jewelrycrafting)
        case "cooking"         => Some(skills.cooking)
        case "alchemy"         => Some(skills.alchemy)
        case _                 => None

  }

  given JsonDecoder[Skill] = JsonDecoder[String].map(Skill.apply)

  case class Item(code: String)

  given JsonDecoder[Item] = JsonDecoder[String].map(Item.apply)

  case class ItemType(code: String)

  given JsonDecoder[ItemType] = JsonDecoder[String].map(ItemType.apply)

  case class Effect(code: String)

  given JsonDecoder[Effect] = JsonDecoder[String].map(Effect.apply)

  def decoder[T: JsonDecoder, K](kf: T => K): JsonDecoder[Map[K, T]] = JsonDecoder[Seq[T]]
    .map(s => Map.from(s.map(v => kf(v) -> v)))
}
