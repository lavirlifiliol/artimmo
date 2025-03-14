package artimmo.model

import zio.json.jsonField

case class Drop(
    code: Code.Item,
    rate: Long,
    @jsonField("min_quantity") minQuant: Long,
    @jsonField("max_quantity") maxQuant: Long
)
