plugins {
    id("java")
    id("org.springframework.boot") version "4.0.1"
}

group = "io.github.douglas-dreer"
version = "0.0.1-SNAPSHOT"
description = "manager-order"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // BOM do Spring Boot (para TODOS os classpaths usados)
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.1"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:4.0.1"))
    testAnnotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:4.0.1"))
    developmentOnly(platform("org.springframework.boot:spring-boot-dependencies:4.0.1"))

    // --- Web & Core ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // --- Data & DB ---
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // --- Messaging ---
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // --- Dev ---
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // --- Testes ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
}


tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.build {
    dependsOn(tasks.test)
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveBaseName.set("manager-order")
    archiveVersion.set(version.toString())
}

tasks.check {
    dependsOn(tasks.test)
}
