package artimmo.client

import artimmo.knowledge.Knowledge
import artimmo.model.actions.common.{Action, Response}
import artimmo.model
import zio.*
import zio.http.{Body, Client, Request, ZClientAspect}
import zio.json.*
import zio.json.ast.{Json, JsonCursor}

import java.time.temporal.ChronoUnit

final class WorldLive(clientIn: Client) extends World {

  private val followRedirects = ZClientAspect.followRedirects(10)((resp, message) => ZIO.logInfo(message).as(resp))

  private val client: Client = clientIn.host(host) @@ followRedirects

  override def get[T](path: String, params: Map[String, String])(implicit t: JsonDecoder[T]): Task[T] = ???

  // TODO let auth refresh knowledge
  override def withAuth(token: String): Task[Account] =
    val nclient = client.addHeader("Authorization", s"Bearer $token")
    val req = Request.get("/my/characters")
    for {
      resp <- nclient.batched(req)
      resp <- resp.body.asString
      chars <- resp.fromJson[UnData[Seq[model.Character]]] match
        case Left(value) => ZIO.fail(Exception(s"couldn't get characters -- $value <== $resp"))
        case Right(value) => ZIO.succeed(value.data)
      ref <- Ref.make(Knowledge(Map.from(chars.map(c => c.information.name -> c))))
    } yield AccountLive(nclient, ref)
}

private case class UnData[T](data: T)

given [T: JsonDecoder] => JsonDecoder[UnData[T]] = DeriveJsonDecoder.gen

final class AccountLive(val client: Client, override val knowledge: Ref[Knowledge]) extends Account {

  override def get[T](path: String, params: Map[String, String])(implicit t: JsonDecoder[T]): Task[T] = ???

  override def withCharacter(character: String): Character = CharacterLive(client, character, knowledge)
}

final class CharacterLive(val client: Client, val charName: String, val knowledge: Ref[Knowledge]) extends Character {

  private val dataCursor = JsonCursor.field("data")
  private val unData = JsonDecoder[Json].mapOrFail(json => json.get(dataCursor))

  override def act[To, From <: Response](action: To)(implicit endpoint: Action[To, From]): Task[Either[String, From]] =
    val body = Body.fromCharSequence(endpoint.encoder.toBody(action))
    val req = Request.post(endpoint.endpoint(charName), body)
    for {
      now <- Clock.currentDateTime
      known <- knowledge.get
      tts <- known.chars.get(charName) match
        case Some(value) => ZIO.succeed(now.until(value.cooldown.expires, ChronoUnit.NANOS))
        case None => ZIO.fail(Exception("Character doesn't exist"))
      _ <- if (tts > 0) ZIO.sleep(tts.nanos) else ZIO.succeed(())
      resp <- client.batched(req)
      str <- resp.body.asString
      resp = (unData mapOrFail endpoint.decoder.fromJsonAST).decodeJson(str)
      _ <- resp match
        case Left(value) => ZIO.succeed(())
        case Right(value) => knowledge.update(k => k.copy(chars = k.chars + (charName -> value.character)))
      // TODO not this
    } yield resp.left.map(e => s"$e <=== $str")
}
