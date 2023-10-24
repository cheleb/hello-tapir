package dev.cheleb.hellotapir

import sttp.tapir.*

import Library.*
import scala.concurrent.Future
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import io.circe.Codec

object Endpoints:
  case class User(name: String) extends AnyVal
  val helloEndpoint: PublicEndpoint[User, Unit, String, Any] = endpoint.get
    .in("hello")
    .in(query[User]("name"))
    .out(stringBody)
  val helloServerEndpoint: ServerEndpoint[Any, Future] = helloEndpoint.serverLogicSuccess(user => Future.successful(s"Hello ${user.name}"))

  val graphvizEndpoint: PublicEndpoint[Graphviz, Unit, GraphvizOutput[_], Any] = endpoint.get
    .in("graphviz")
    .in(header[Graphviz]("OutputFormat"))
    .out(
      oneOf[GraphvizOutput[_]](
        oneOfVariant(jsonBody[GraphvizOutput.SVG]),
        oneOfVariant(jsonBody[GraphvizOutput.PNG]),
        oneOfVariant(plainBody[GraphvizOutput.DOT])
      )
    )

  val graphvizServerEndpoint: ServerEndpoint[Any, Future] =
    graphvizEndpoint.serverLogicSuccess {
      case Graphviz.SVG => Future.successful(GraphvizOutput.SVG("svg"))
      case Graphviz.PNG => Future.successful(GraphvizOutput.PNG("png"))
      case Graphviz.DOT => Future.successful(GraphvizOutput.DOT("dot"))
    }

  val booksListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint.get
    .in("books" / "list" / "all")
    .out(jsonBody[List[Book]])
  val booksListingServerEndpoint: ServerEndpoint[Any, Future] = booksListing.serverLogicSuccess(_ => Future.successful(Library.books))

  val apiEndpoints: List[ServerEndpoint[Any, Future]] = List(helloServerEndpoint, booksListingServerEndpoint, graphvizServerEndpoint)

  val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "yammering-mouse", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
  val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)

object Library:
  case class Author(name: String) derives Codec.AsObject
  case class Book(title: String, year: Int, author: Author) derives Codec.AsObject

  val books = List(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
