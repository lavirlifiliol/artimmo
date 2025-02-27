package artimmo.client

import artimmo.knowledge.Knowledge
import zio.*
import zio.http.{Body, Client, Request, ZClientAspect}
import zio.json.{JsonDecoder, JsonEncoder}
import zio.json.ast.{Json, JsonCursor}
import artimmo.model
import artimmo.model.actions.internals.{Action, ReqEncoder, Response, given}

import java.time.Instant

trait World {
  protected val host = "api.artifactsmmo.com"


  // TODO not this
  def get[T](path: String, params: Map[String, String])(implicit t: JsonDecoder[T]): Task[T]

  def withAuth(token: String): Task[Account]
}

// Narrow down the error types, most of these are *not* Throwable

object World {
  val live: ZLayer[Client, Throwable, World] = ZLayer(for {
    client <- ZIO.service[Client]
  } yield WorldLive(client))
}

trait Account {
  val knowledge: Ref[Knowledge]

  def get[T](path: String, params: Map[String, String])(implicit t: JsonDecoder[T]): Task[T]

  def withCharacter(character: String): Character
}

trait Character {
  // TODO encode errors
  def act[To, From <: Response](action: To)(implicit endpoint: Action[To, From]): Task[Either[String, From]]
}