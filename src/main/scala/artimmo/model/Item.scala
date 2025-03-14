package artimmo.model

import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}
import Item.given

case class Item(
    name: String,
    code: Code.Item,
    level: Long,
    @jsonField("type") itemType: Code.ItemType,
    // @jsonField("subtype") itemSubType: String,
    // description: String,
    effects: Map[Code.Effect, Effect],
    craft: Option[Item.Recipe],
    tradable: Boolean
)

object Item {
  case class Recipe(skill: Code.Skill, level: Long, items: Map[Code.Item, Long], quantity: Long)

  given effectsJson: JsonDecoder[Map[Code.Effect, Effect]] = Code.decoder(_.code)

  private case class Ingredient(code: Code.Item, quantity: Long)

  private given JsonDecoder[Ingredient] = DeriveJsonDecoder.gen

  given ingredientsJson: JsonDecoder[Map[Code.Item, Long]] = JsonDecoder[Seq[Ingredient]]
    .map(seq => Map.from(seq.map(i => i.code -> i.quantity)))

  given JsonDecoder[Recipe] = DeriveJsonDecoder.gen
}

given JsonDecoder[Item] = DeriveJsonDecoder.gen
