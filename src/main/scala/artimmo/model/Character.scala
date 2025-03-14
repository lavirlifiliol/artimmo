package artimmo.model

import zio.json.*
import zio.json.ast.{Json, JsonCursor}

import java.time.{Instant, OffsetDateTime}

case class Level(level: Long, xp: Long, maxXp: Long)

@jsonMemberNames(CustomCase(SnakeCase andThen (_ + "_slot")))
case class EquipmentSlots(
    weapon: String,
    rune: String,
    shield: String,
    helmet: String,
    bodyArmor: String,
    legArmor: String,
    boots: String,
    ring1: String,
    ring2: String,
    amulet: String,
    artifact1: String, // TODO fixed arrays
    artifact2: String,
    artifact3: String,
    utility1: String,
    @jsonField("utility1_slot_quantity") utility1Quant: Long,
    utility2: String,
    @jsonField("utility2_slot_quantity") utility2Quant: Long,
    bag: String
)

case class ElementalStats(attack: Long, dmg: Long, res: Long)

case class Inventory(@jsonField("inventory_max_items") capacity: Long, inventory: IndexedSeq[Slot])

case class Slot(slot: Long, code: String, @jsonField("quantity") quant: Long)

object Slot {
  implicit val jsonDecoder: JsonDecoder[Slot] = DeriveJsonDecoder.gen[Slot]
}

@jsonMemberNames(SnakeCase)
case class Information(
    name: String,
    account: String,
    skin: String,
    gold: Long,
    speed: Long,
    hp: Long,
    max_hp: Long,
    haste: Long,
    criticalStrike: Long,
    wisdom: Long,
    prospecting: Long,
    dmg: Long,
    x: Long,
    y: Long
)

val decoder = DeriveJsonDecoder.gen[Information]

case class CharCooldown(
    @jsonField("cooldown_expiration") expires: OffsetDateTime /*, @jsonField("cooldown") seconds: Long */
)

case class Skills(
    mining: Level,
    woodcutting: Level,
    fishing: Level,
    weaponcrafting: Level,
    gearcrafting: Level,
    jewelrycrafting: Level,
    cooking: Level,
    alchemy: Level
)

@jsonMemberNames(SnakeCase)
case class Task(task: String, taskType: String, taskProgress: Long, taskTotal: Long)

case class Character(
    information: Information,
    level: Level,
    skills: Skills,
    fire: ElementalStats,
    earth: ElementalStats,
    water: ElementalStats,
    air: ElementalStats,
    cooldown: CharCooldown,
    equipment: EquipmentSlots,
    task: Option[Task],
    inventory: Inventory
)

object Character {
  private val asLong = JsonDecoder[Long]

  private def levelDecoder(prefix: String): JsonDecoder[Level] =
    val levelC = JsonCursor.field(prefix + "level") >>> JsonCursor.isNumber
    val xpC = JsonCursor.field(prefix + "xp") >>> JsonCursor.isNumber
    val maxXpC = JsonCursor.field(prefix + "max_xp") >>> JsonCursor.isNumber
    JsonDecoder[Json].mapOrFail(json =>
      for {
        levelN <- json.get(levelC)
        xpN <- json.get(xpC)
        maxXpN <- json.get(maxXpC)
        level <- asLong.fromJsonAST(levelN)
        xp <- asLong.fromJsonAST(xpN)
        maxXp <- asLong.fromJsonAST(maxXpN)
      } yield Level(level, xp, maxXp)
    )

  private def elementalDecoder(suffix: String): JsonDecoder[ElementalStats] =
    val attackC = JsonCursor.field("attack" + suffix) >>> JsonCursor.isNumber
    val dmgC = JsonCursor.field("dmg" + suffix) >>> JsonCursor.isNumber
    val resC = JsonCursor.field("res" + suffix) >>> JsonCursor.isNumber
    JsonDecoder[Json].mapOrFail(json =>
      for {
        attack <- json.get(attackC) flatMap asLong.fromJsonAST
        dmg <- json.get(dmgC) flatMap asLong.fromJsonAST
        res <- json.get(dmgC) flatMap asLong.fromJsonAST
      } yield ElementalStats(attack, dmg, res)
    )

  private val allLevels =
    levelDecoder("mining_") both levelDecoder("woodcutting_") both levelDecoder("fishing_") both
      levelDecoder("weaponcrafting_") both levelDecoder("gearcrafting_") both levelDecoder("jewelrycrafting_") both
      levelDecoder("cooking_") both levelDecoder("alchemy_")
  private val skillsDecoder = allLevels.map(skills =>
    val (((((((mining, woodcutting), fishing), weaponcrafting), gearcrafting), jewelrycrafting), cooking), alchemy) =
      skills
    Skills(mining, woodcutting, fishing, weaponcrafting, gearcrafting, jewelrycrafting, cooking, alchemy)
  )
  implicit val jsonDecoder: JsonDecoder[Character] = JsonDecoder[Json].mapOrFail(json =>
    for {
      info <- DeriveJsonDecoder.gen[Information].fromJsonAST(json)
      level <- levelDecoder("").fromJsonAST(json)
      skills <- skillsDecoder.fromJsonAST(json)
      fire <- elementalDecoder("_fire").fromJsonAST(json)
      earth <- elementalDecoder("_earth").fromJsonAST(json)
      water <- elementalDecoder("_water").fromJsonAST(json)
      air <- elementalDecoder("_air").fromJsonAST(json)
      cooldown <- DeriveJsonDecoder.gen[CharCooldown].fromJsonAST(json)
      task <- DeriveJsonDecoder.gen[Task].map(t => if (t.task != "") Some(t) else None).fromJsonAST(json)
      equipment <- DeriveJsonDecoder.gen[EquipmentSlots].fromJsonAST(json)
      inventory <- DeriveJsonDecoder.gen[Inventory].fromJsonAST(json)
    } yield Character(info, level, skills, fire, earth, water, air, cooldown, equipment, task, inventory)
  )
}
