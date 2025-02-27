package artimmo

import zio.*
import zio.http.*

import artimmo.model.actions._
import artimmo.model.actions.given

val TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImFkYW0uaHJiYWNAZ21haWwuY29tIiwicGFzc3dvcmRfY2hhbmdlZCI6IiJ9.O6YyhHvX0cDbg-Bf3FeEJ0Aj49e3RVbpOr8-2C7H80o"

object MyApp extends ZIOAppDefault:
  val app =
    for {
      world <- ZIO.service[client.World]
      account <- world.withAuth(TOKEN)
      character = account.withCharacter("primus")
      _ <- (for {
        _ <- character.act(Rest) flatMap (f => Console.printLine(f.map(_.hpRestored)))
        _ <- character.act(Fight) flatMap (f => Console.printLine(f.map(_.fight)))
      } yield ()) repeat Schedule.forever
    } yield ()

  def run = app.provide(Client.default, client.World.live)

//