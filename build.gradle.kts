plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    id("org.sonarqube") version "7.2.2.6593"
}

group = "io.github.douglas-dreer"
version = "0.0.1-SNAPSHOT"
description = "manager-order"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "douglas-dreer_manager-order")
        property("sonar.organization", "douglas-dreer")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    testCompileOnly {
        extendsFrom(configurations.testAnnotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // --- Spring Boot Starters ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // --- Database ---
    runtimeOnly("org.postgresql:postgresql")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // --- Development Tools ---
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // --- Resilience4j (Spring Boot 4 compatível) ---
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // --- Test Dependencies ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")

    // --- Testcontainers ---
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
}

tasks.withType<Test> {
    useJUnitPlatform()

    systemProperty("spring.profiles.active", "test")

    systemProperty("testcontainers.reuse.enable", "false")

    systemProperty("spring.datasource.hikari.max-lifetime", "10000")
    systemProperty("spring.datasource.hikari.connection-timeout", "5000")
    systemProperty("spring.datasource.hikari.validation-timeout", "2000")

    jvmArgs = listOf(
        "-XX:+EnableDynamicAgentLoading",
        "-Djdk.instrument.traceUsage=false"
    )

    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.bootJar {
    archiveBaseName.set("manager-order")
    archiveVersion.set(version.toString())
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

tasks.jar {
    enabled = false
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked"))
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform {
        includeTags("integration")
    }
}

springBoot {
    buildInfo()
}