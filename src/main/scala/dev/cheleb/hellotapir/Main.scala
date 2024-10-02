package dev.cheleb.hellotapir

import sttp.tapir.ztapir.*

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import ExecutionContext.Implicits.global
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import zio.http.Server
import zio.http.HttpApp
import zio.*
import zio.logging.backend.SLF4J
import zio.logging.LogFormat

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = SLF4J.slf4j(LogLevel.Debug, LogFormat.default)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =

    val serverOptions = ZioHttpServerOptions.customiseInterceptors
      .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
      .defaultHandlers(myFailureResponse)
      .options

    val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)

    val app: HttpApp[Any] = ZioHttpInterpreter(serverOptions).toHttp(Endpoints.all)

    (for {
      actualPort <- Server.install(app) // or .serve if you don't need the port and want to keep it running without manual readLine
      _ <- zio.Console.printLine(s"Go to http://localhost:${actualPort}/docs to open SwaggerUI. Press ENTER key to exit.")
      _ <- zio.Console.readLine
    } yield ())
      .provide(
        ZLayer.succeed(Server.Config.default.port(port)),
        Server.live
      )
      .exitCode
