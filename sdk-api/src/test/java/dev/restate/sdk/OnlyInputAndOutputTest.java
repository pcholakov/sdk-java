// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate Java SDK,
// which is released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/sdk-java/blob/main/LICENSE
package dev.restate.sdk;

import dev.restate.sdk.core.OnlyInputAndOutputTestSuite;
import dev.restate.sdk.core.testservices.GreeterGrpc;
import dev.restate.sdk.core.testservices.GreetingRequest;
import dev.restate.sdk.core.testservices.GreetingResponse;
import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

public class OnlyInputAndOutputTest extends OnlyInputAndOutputTestSuite {

  private static class NoSyscallsGreeter extends GreeterGrpc.GreeterImplBase
      implements RestateService {
    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
      responseObserver.onNext(
          GreetingResponse.newBuilder().setMessage("Hello " + request.getName()).build());
      responseObserver.onCompleted();
    }
  }

  @Override
  protected BindableService noSyscallsGreeter() {
    return new NoSyscallsGreeter();
  }
}
