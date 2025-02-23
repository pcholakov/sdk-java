import com.google.protobuf.gradle.id

plugins {
  `java-library`
  `library-publishing-conventions`
}

description = "Restate SDK APIs"

dependencies {
  api(project(":sdk-common"))

  testCompileOnly(coreLibs.javax.annotation.api)

  testImplementation(project(":sdk-core"))
  testImplementation(testingLibs.junit.jupiter)
  testImplementation(testingLibs.assertj)
  testImplementation(coreLibs.protobuf.java)
  testImplementation(coreLibs.grpc.stub)
  testImplementation(coreLibs.grpc.protobuf)
  testImplementation(coreLibs.log4j.core)

  // Import test suites from sdk-core
  testImplementation(project(":sdk-core", "testArchive"))
  testProtobuf(project(":sdk-core", "testArchive"))
}

val pluginJar =
    file(
        "${project.rootProject.rootDir}/protoc-gen-restate/build/libs/protoc-gen-restate-${project.version}-all.jar")

protobuf {
  plugins {
    id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:${coreLibs.versions.grpc.get()}" }
    id("restate") {
      // NOTE: This is not needed in a regular project configuration, you should rather use:
      // artifact = "dev.restate.sdk:protoc-gen-restate-java-blocking:1.0-SNAPSHOT:all@jar"
      path = pluginJar.path
    }
  }

  generateProtoTasks {
    ofSourceSet("test").forEach {
      // Make sure we depend on shadowJar from protoc-gen-restate
      it.dependsOn(":protoc-gen-restate:shadowJar")

      it.plugins {
        id("grpc")
        id("restate")
      }
    }
  }
}

// Generate test jar

configurations { register("testArchive") }

tasks.register<Jar>("testJar") {
  archiveClassifier.set("tests")

  from(project.the<SourceSetContainer>()["test"].output)
}

artifacts { add("testArchive", tasks["testJar"]) }
