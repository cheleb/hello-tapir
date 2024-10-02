package dev.cheleb.hellotapir

import sttp.tapir.ztapir.*
import sttp.tapir.PublicEndpoint
import Library.*
import scala.concurrent.Future
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import io.circe.Codec
import zio.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter

object Endpoints:
  case class User(name: String) extends AnyVal
  val helloEndpoint: PublicEndpoint[User, Unit, String, Any] = endpoint.get
    .in("hello")
    .in(query[User]("name"))
    .out(stringBody)

  val helloServerEndpoint: ZServerEndpoint[Any, Any] = helloEndpoint.serverLogicSuccess(user => ZIO.succeed(s"Hello ${user.name}"))

  val intEndpoint: PublicEndpoint[Int, Unit, String, Any] = endpoint.get
    .in("int")
    .in(query[Int]("value"))
    .out(stringBody)

  val intServerEndpoint: ZServerEndpoint[Any, Any] = intEndpoint
    .serverLogicSuccess(value => ZIO.succeed(s"Value is $value"))

  val graphvizEndpoint: PublicEndpoint[(Graphviz, String), Unit, GraphvizOutput[_], Any] = endpoint.get
    .in("graphviz")
    .in(header[Graphviz]("OutputFormat"))
    .in(query[String]("zozo"))
    .out(
      oneOf[GraphvizOutput[_]](
        oneOfVariant(jsonBody[GraphvizOutput.SVG]),
        oneOfVariant(jsonBody[GraphvizOutput.PNG]),
        oneOfVariant(plainBody[GraphvizOutput.DOT])
      )
    )

  val graphvizServerEndpoint: ZServerEndpoint[Any, Any] =
    graphvizEndpoint.serverLogicSuccess { case (ss, _) =>
      ss match {
        case Graphviz.SVG => ZIO.succeed(GraphvizOutput.SVG("svg"))
        case Graphviz.PNG => ZIO.succeed(GraphvizOutput.PNG("png"))
        case Graphviz.DOT => ZIO.succeed(GraphvizOutput.DOT("dot"))
      }
    }

  val booksListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint.get
    .in("books" / "list" / "all")
    .out(jsonBody[List[Book]])
  val booksListingServerEndpoint: ZServerEndpoint[Any, Any] = booksListing.serverLogicSuccess(_ => ZIO.succeed(Library.books))

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(helloServerEndpoint, intServerEndpoint, booksListingServerEndpoint, graphvizServerEndpoint)

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "spiritual-marten", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)

object Library:
  case class Author(name: String) derives Codec.AsObject
  case class Book(title: String, year: Int, author: Author) derives Codec.AsObject

  val books = List(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
