plugins {
    kotlin("jvm") version "1.9.22"
    antlr
    idea
}

group = "org.example"
version = "1.0-SNAPSHOT"

val antlr4 = "org.antlr:antlr4"
val antlrVersion = "4.9.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    antlr("$antlr4:$antlrVersion")
    compileOnly("$antlr4-runtime:$antlrVersion")
}

tasks.test {
    useJUnitPlatform()
}
tasks.compileKotlin {
    dependsOn("generateGrammarSource")
}
tasks.generateGrammarSource {
    maxHeapSize = "128m"
    arguments = arguments + listOf("-visitor", "-listener")
}
kotlin {
    jvmToolchain(21)
}
