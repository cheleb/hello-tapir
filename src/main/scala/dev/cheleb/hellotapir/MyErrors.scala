package dev.cheleb.hellotapir

import sttp.tapir._
import sttp.tapir.server.model.ValuedEndpointOutput

import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import io.circe.Codec as CirceCodec

case class MyFailure(msg: String) derives CirceCodec.AsObject, Schema

def myFailureResponse(m: String): ValuedEndpointOutput[_] =
  ValuedEndpointOutput(jsonBody[MyFailure], MyFailure(m))

val myServerOptions: ZioHttpServerOptions[Any] = ZioHttpServerOptions.customiseInterceptors
//  .decodeFailureHandler(myFailureResponse)
  .defaultHandlers(myFailureResponse)
  .options
