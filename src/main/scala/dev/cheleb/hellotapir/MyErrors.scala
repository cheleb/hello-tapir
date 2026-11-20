package dev.cheleb.hellotapir

import sttp.tapir.*
import sttp.tapir.server.model.ValuedEndpointOutput

import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import io.circe.Codec as CirceCodec

case class MyFailure(msg: String) derives CirceCodec.AsObject, Schema

def myFailureResponse(m: String): ValuedEndpointOutput[?] =
  ValuedEndpointOutput(jsonBody[MyFailure], MyFailure(m))

val myServerOptions: ZioHttpServerOptions[Any] = ZioHttpServerOptions.customiseInterceptors
//  .decodeFailureHandler(myFailureResponse)
  .defaultHandlers(myFailureResponse)
  .options
