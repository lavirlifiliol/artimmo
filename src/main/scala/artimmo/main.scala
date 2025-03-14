package artimmo

import zio.*
import zio.http.*
import artimmo.model.actions.*
import artimmo.model.actions.given

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.Try

object MyApp extends ZIOAppDefault:
  val app =
    for {
      world <- ZIO.service[client.World]
      token <- ZIO.fromTry(Try(Files.readString(Path.of("token.txt"), StandardCharsets.US_ASCII).trim))
      account <- world.withAuth(token)
      character = account.withCharacter("primus")
      _ <- (for {
        _ <- character.act(Rest) flatMap (f => Console.printLine(f.map(_.hpRestored)))
        _ <- character.act(Fight) flatMap (f => Console.printLine(f.map(_.fight)))
      } yield ()) repeat Schedule.forever
    } yield ()

  def run: ZIO[Any, Throwable, Unit] = app.provide(Client.default, client.World.live)

//
