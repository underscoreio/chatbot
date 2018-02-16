package io.underscore.chatbot

import cats.syntax.all._
import cats.effect.{IO,Sync}
import fs2.{async,Pipe,Stream,StreamApp,Sink}
import fs2.async.mutable.Topic
import java.io.File
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits.{Text,WebSocketFrame}
import scala.concurrent.ExecutionContext.Implicits.global

object ChatServer extends StreamApp[IO] with Http4sDsl[IO] {
  val chatTopic: Topic[IO,String] =
    async.topic("Welcome to chat").unsafeRunSync()

  val service = HttpService[IO] {
    case request @ GET -> Root / "index.html" =>
      StaticFile.fromFile(new File("site/index.html"), Some(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist

    case GET -> Root / "chat" / _ =>
      val toClient: Stream[IO, WebSocketFrame] =
        chatTopic.subscribe(100).map(t => Text(s"chat $t"))
      val fromClient: Sink[IO, WebSocketFrame] =
        source => source.
          collect{ case Text(t, _) => t }.
          through(debug[IO,String]("chat")).
          to(chatTopic.publish)

      WebSocketBuilder[IO].build(toClient, fromClient)
  }

  def debug[F[_],A](prefix: String)(implicit sync: Sync[F]): Pipe[F,A,A] =
    in => in.evalMap(a => sync.suspend(println(s"$prefix: $a").pure[F]).map(_ => a))

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/")
      .serve
}
