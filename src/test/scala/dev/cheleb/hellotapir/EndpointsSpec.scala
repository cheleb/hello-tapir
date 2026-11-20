package dev.cheleb.hellotapir

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.model.Uri
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*

class EndpointsSpec extends AnyFunSuite with Matchers:

  private def get(url: String, headers: Map[String, String] = Map.empty): Response[Either[String, String]] =
    val backend = TapirStubInterpreter(SttpBackendStub[Task, Any](new RIOMonadError[Any]))
      .whenServerEndpointsRunLogic(Endpoints.apiEndpoints)
      .backend()
    val req = basicRequest.get(Uri.unsafeParse(s"http://test.com/$url")).headers(headers)
    Unsafe.unsafe { implicit u =>
      Runtime.default.unsafe.run(req.send(backend)).getOrThrowFiberFailure()
    }

  test("hello endpoint greets the user"):
    val r = get("hello?name=Bob")
    r.code.code shouldBe 200
    r.body shouldBe Right("Hello Bob")

  test("int endpoint echoes the value"):
    val r = get("int?value=42")
    r.code.code shouldBe 200
    r.body shouldBe Right("Value is 42")

  test("int endpoint rejects a non-integer value"):
    get("int?value=abc").code.code shouldBe 400

  test("hello endpoint rejects a missing name"):
    get("hello").code.code shouldBe 400

  test("books listing returns all books"):
    val r = get("books/list/all")
    r.code.code shouldBe 200
    io.circe.parser.decode[List[Library.Book]](r.body.toOption.get) shouldBe Right(Library.books)

  test("graphviz endpoint returns SVG as JSON"):
    val r = get("graphviz?zozo=x", Map("OutputFormat" -> "SVG"))
    r.code.code shouldBe 200
    r.body shouldBe Right("""{"value":"svg"}""")

  test("graphviz endpoint returns PNG as JSON"):
    val r = get("graphviz?zozo=x", Map("OutputFormat" -> "PNG"))
    r.code.code shouldBe 200
    r.body shouldBe Right("""{"value":"png"}""")

  test("graphviz endpoint returns DOT as plain text"):
    val r = get("graphviz?zozo=x", Map("OutputFormat" -> "DOT"))
    r.code.code shouldBe 200
    r.body shouldBe Right("dot")
