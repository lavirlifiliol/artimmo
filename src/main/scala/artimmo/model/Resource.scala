package artimmo.model

case class Resource(name: String, code: Code.Resource, skill: Code.Skill, level: Long, drops: Seq[Drop])
