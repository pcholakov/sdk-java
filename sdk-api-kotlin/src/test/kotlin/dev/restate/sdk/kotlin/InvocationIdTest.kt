// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate Java SDK,
// which is released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/sdk-java/blob/main/LICENSE
package dev.restate.sdk.kotlin

import dev.restate.sdk.common.InvocationId
import dev.restate.sdk.core.InvocationIdTestSuite
import dev.restate.sdk.core.testservices.GreeterGrpcKt
import dev.restate.sdk.core.testservices.GreetingRequest
import dev.restate.sdk.core.testservices.GreetingResponse
import dev.restate.sdk.core.testservices.greetingResponse
import io.grpc.BindableService
import kotlinx.coroutines.Dispatchers

class InvocationIdTest : InvocationIdTestSuite() {
  private class ReturnInvocationId :
      GreeterGrpcKt.GreeterCoroutineImplBase(Dispatchers.Unconfined), RestateKtService {
    override suspend fun greet(request: GreetingRequest): GreetingResponse {
      return greetingResponse { message = InvocationId.current().toString() }
    }
  }

  override fun returnInvocationId(): BindableService {
    return ReturnInvocationId()
  }
}
