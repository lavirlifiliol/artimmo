package artimmo.datalog

import edu.harvard.seas.pl.abcdatalog.engine.DatalogEngine
import edu.harvard.seas.pl.abcdatalog.ast.*
import edu.harvard.seas.pl.abcdatalog.engine.bottomup.sequential.SemiNaiveEngine

import java.util

class Datalog {
  private val engine: DatalogEngine = SemiNaiveEngine(false)
  val foo = Clause(
    PositiveAtom.create(PredicateSym.create("foo", 2), Array(Constant.create("foo"), Constant.create("foo"))),
    util.ArrayList[Premise]()
  )
}
