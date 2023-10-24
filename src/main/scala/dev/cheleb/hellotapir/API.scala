package dev.cheleb.hellotapir

import sttp.tapir.Schema
import sttp.tapir.Codec
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.DecodeResult
import cats.Show
import cats.syntax.all.*

import java.util.Locale
import io.circe.Codec as CirceCodec

enum Graphviz:
  case SVG
  case PNG
  case DOT

object Graphviz:
  given show: Show[Graphviz] = _.toString.toLowerCase(Locale.ROOT)

  given schema: Schema[Graphviz] =
    Schema.derivedEnumeration.defaultStringBased
  given tapirCodec: Codec[String, Graphviz, TextPlain] =
    Codec.derivedEnumeration.defaultStringBased

sealed trait GraphvizOutput[+T <: Graphviz]

object GraphvizOutput:
  case class SVG(value: String) extends GraphvizOutput[Graphviz.SVG.type] derives CirceCodec.AsObject, Schema
  case class PNG(value: String) extends GraphvizOutput[Graphviz.PNG.type] derives CirceCodec.AsObject, Schema
  case class DOT(value: String) extends GraphvizOutput[Graphviz.DOT.type] derives CirceCodec.AsObject, Schema

  object DOT:
    given tapirCodec: Codec[String, DOT, TextPlain] =
      Codec.string.mapDecode(str => DecodeResult.Value(DOT(str)))(dot => dot.value)
    given schema: Schema[DOT] = Schema.derived
