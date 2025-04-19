import org.gradle.internal.deprecation.DeprecatableConfiguration

plugins {
    id("java")
    id("org.springframework.boot") version "3.4.4"
    id("gg.jte.gradle") version "3.1.16"
}

group = "ru.capybarovsk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-jetty:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-security:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.4") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // implementation("com.openai:openai-java:0.40.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
    implementation("gg.jte:jte:3.1.16")
    implementation("gg.jte:jte-spring-boot-starter-3:3.1.16")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.postgresql:postgresql:42.7.2")
}

jte {
    precompile()
}

tasks.bootJar {
    dependsOn(tasks.precompileJte)
    with(bootInf) {
        from(fileTree("jte-classes") {
            include("**/*.class")
        })
        into("classes")
    }
}

fun Configuration.isDeprecated() = this is DeprecatableConfiguration

fun ConfigurationContainer.resolveAll() = this
    .filter { it.isCanBeResolved && !it.isDeprecated() }
    .forEach { it.resolve() }

tasks.register("downloadDependencies") {
    doLast {
        configurations.resolveAll()
        buildscript.configurations.resolveAll()
    }
}

springBoot {
    mainClass.set("ru.capybarovsk.overhaul.Application")
}