// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate Java SDK,
// which is released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/sdk-java/blob/main/LICENSE
package dev.restate.sdk.testing;

import dev.restate.sdk.common.BlockingService;
import dev.restate.sdk.common.NonBlockingService;
import dev.restate.sdk.http.vertx.RestateHttpEndpointBuilder;
import io.grpc.ServerInterceptor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/** Builder for {@link RestateRunner}. See {@link RestateRunner} for more details. */
public class RestateRunnerBuilder {

  private static final String DEFAULT_RESTATE_CONTAINER = "docker.io/restatedev/restate";
  private final RestateHttpEndpointBuilder endpointBuilder;
  private String restateContainerImage = DEFAULT_RESTATE_CONTAINER;
  private final Map<String, String> additionalEnv = new HashMap<>();
  private String configFile;

  RestateRunnerBuilder(RestateHttpEndpointBuilder endpointBuilder) {
    this.endpointBuilder = endpointBuilder;
  }

  /** Override the container image to use for the Restate runtime. */
  public RestateRunnerBuilder withRestateContainerImage(String restateContainerImage) {
    this.restateContainerImage = restateContainerImage;
    return this;
  }

  /** Add additional environment variables to the Restate container. */
  public RestateRunnerBuilder withAdditionalEnv(String key, String value) {
    this.additionalEnv.put(key, value);
    return this;
  }

  /** Mount a config file in the Restate container. */
  public RestateRunnerBuilder withConfigFile(String configFile) {
    this.configFile = configFile;
    return this;
  }

  /**
   * Register a service. See {@link RestateHttpEndpointBuilder#withService(BlockingService,
   * ServerInterceptor...)}.
   */
  public RestateRunnerBuilder withService(
      BlockingService service, ServerInterceptor... interceptors) {
    this.endpointBuilder.withService(service, interceptors);
    return this;
  }

  /**
   * Register a service. See {@link RestateHttpEndpointBuilder#withService(BlockingService,
   * Executor, ServerInterceptor...)}.
   */
  public RestateRunnerBuilder withService(
      BlockingService service, Executor executor, ServerInterceptor... interceptors) {
    this.endpointBuilder.withService(service, executor, interceptors);
    return this;
  }

  /**
   * Register a service. See {@link RestateHttpEndpointBuilder#withService(NonBlockingService,
   * ServerInterceptor...)}.
   */
  public RestateRunnerBuilder withService(
      NonBlockingService service, ServerInterceptor... interceptors) {
    this.endpointBuilder.withService(service, interceptors);
    return this;
  }

  public ManualRestateRunner buildManualRunner() {
    return new ManualRestateRunner(
        this.endpointBuilder.build(),
        this.restateContainerImage,
        this.additionalEnv,
        this.configFile);
  }

  public RestateRunner buildRunner() {
    return new RestateRunner(this.buildManualRunner());
  }

  public static RestateRunnerBuilder create() {
    return new RestateRunnerBuilder(RestateHttpEndpointBuilder.builder());
  }

  /** Create from {@link RestateHttpEndpointBuilder}. */
  public static RestateRunnerBuilder of(RestateHttpEndpointBuilder endpointBuilder) {
    return new RestateRunnerBuilder(endpointBuilder);
  }
}
