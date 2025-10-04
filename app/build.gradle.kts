import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("chirp.spring-boot-app")
}

group = "com.vsv"
version = "0.0.1-SNAPSHOT"
description = "Chirp Backend"

tasks {
    named<BootJar>("bootJar") {
        from(project(":notification").projectDir.resolve("src/main/resources")) {
            into("")
        }
        from(project(":user").projectDir.resolve("src/main/resources")) {
            into("")
        }
    }
}

dependencies {
    implementation(projects.user)
    implementation(projects.chat)
    implementation(projects.notification)
    implementation(projects.common)

    implementation(libs.kotlin.reflect)
//    implementation(libs.jackson.module.kotlin)
//    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.amqp)
    runtimeOnly(libs.postgresql)
}
