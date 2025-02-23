// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate Java SDK,
// which is released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/sdk-java/blob/main/LICENSE
package dev.restate.sdk.core;

import static dev.restate.sdk.core.AssertUtils.*;
import static dev.restate.sdk.core.ProtoUtils.*;
import static dev.restate.sdk.core.TestDefinitions.*;
import static org.assertj.core.api.Assertions.assertThat;

import dev.restate.generated.sdk.java.Java;
import dev.restate.sdk.common.TerminalException;
import dev.restate.sdk.core.testservices.GreeterGrpc;
import dev.restate.sdk.core.testservices.GreetingRequest;
import io.grpc.BindableService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public abstract class UserFailuresTestSuite implements TestSuite {

  public static final String MY_ERROR = "my error";

  public static final String WHATEVER = "Whatever";

  protected abstract BindableService throwIllegalStateException();

  protected abstract BindableService sideEffectThrowIllegalStateException(
      AtomicInteger nonTerminalExceptionsSeen);

  protected abstract BindableService throwTerminalException(
      TerminalException.Code code, String message);

  protected abstract BindableService sideEffectThrowTerminalException(
      TerminalException.Code code, String message);

  @Override
  public Stream<TestDefinition> definitions() {
    AtomicInteger nonTerminalExceptionsSeen = new AtomicInteger();

    return Stream.of(
        // Cases returning ErrorMessage
        testInvocation(this::throwIllegalStateException, GreeterGrpc.getGreetMethod())
            .withInput(startMessage(1), inputMessage(GreetingRequest.getDefaultInstance()))
            .assertingOutput(
                AssertUtils.containsOnlyExactErrorMessage(new IllegalStateException("Whatever"))),
        testInvocation(
                () -> this.sideEffectThrowIllegalStateException(nonTerminalExceptionsSeen),
                GreeterGrpc.getGreetMethod())
            .withInput(startMessage(1), inputMessage(GreetingRequest.getDefaultInstance()))
            .assertingOutput(
                msgs -> {
                  AssertUtils.containsOnly(
                      AssertUtils.exactErrorMessage(new IllegalStateException("Whatever")));

                  // Check the counter has not been incremented
                  assertThat(nonTerminalExceptionsSeen).hasValue(0);
                }),

        // Cases completing the invocation with OutputStreamEntry.failure
        testInvocation(
                () -> this.throwTerminalException(TerminalException.Code.INTERNAL, MY_ERROR),
                GreeterGrpc.getGreetMethod())
            .withInput(startMessage(1), inputMessage(GreetingRequest.getDefaultInstance()))
            .expectingOutput(outputMessage(TerminalException.Code.INTERNAL, MY_ERROR))
            .named("With internal error"),
        testInvocation(
                () -> this.throwTerminalException(TerminalException.Code.UNKNOWN, WHATEVER),
                GreeterGrpc.getGreetMethod())
            .withInput(startMessage(1), inputMessage(GreetingRequest.getDefaultInstance()))
            .expectingOutput(outputMessage(TerminalException.Code.UNKNOWN, WHATEVER))
            .named("With unknown error"),
        testInvocation(
                () ->
                    this.sideEffectThrowTerminalException(
                        TerminalException.Code.INTERNAL, MY_ERROR),
                GreeterGrpc.getGreetMethod())
            .withInput(
                startMessage(1), inputMessage(GreetingRequest.getDefaultInstance()), ackMessage(1))
            .expectingOutput(
                Java.SideEffectEntryMessage.newBuilder()
                    .setFailure(Util.toProtocolFailure(TerminalException.Code.INTERNAL, MY_ERROR)),
                outputMessage(TerminalException.Code.INTERNAL, MY_ERROR))
            .named("With internal error"),
        testInvocation(
                () ->
                    this.sideEffectThrowTerminalException(TerminalException.Code.UNKNOWN, WHATEVER),
                GreeterGrpc.getGreetMethod())
            .withInput(
                startMessage(1), inputMessage(GreetingRequest.getDefaultInstance()), ackMessage(1))
            .expectingOutput(
                Java.SideEffectEntryMessage.newBuilder()
                    .setFailure(Util.toProtocolFailure(TerminalException.Code.UNKNOWN, WHATEVER)),
                outputMessage(TerminalException.Code.UNKNOWN, WHATEVER))
            .named("With unknown error"));
  }
}
