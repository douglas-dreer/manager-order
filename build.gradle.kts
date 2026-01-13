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
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true) // O SonarCloud requer o formato XML
        html.required.set(true)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "douglas-dreer_pokemon-tcg-collection")
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
    // --- Spring Boot Starters (Spring Boot 4) ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Starter específico para JPA
    implementation("org.springframework.boot:spring-boot-starter-amqp")     // Starter específico para AMQP

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

    // --- Test Dependencies (CRÍTICO para Spring Boot 4) ---
    // Starter de teste base
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Starters de teste específicos para cada tecnologia usada
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-amqp-test")
    // Módulo para integração com Testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    // Testcontainers
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
}

tasks {
    test {
        useJUnitPlatform()
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

    bootJar {
        archiveBaseName.set("manager-order")
        archiveVersion.set(version.toString())
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }

    jar {
        enabled = false
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked"))
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }
}

springBoot {
    buildInfo()
}