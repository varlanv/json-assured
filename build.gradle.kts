plugins {
    id("java")
    id("com.github.spotbugs") version "6.1.11"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.varlanv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.named<JavaCompile>("compileJava", {
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-Werror")
    options.release = 11
    finalizedBy("spotbugsMain")
})

tasks.named<JavaCompile>("compileTestJava", {
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-Werror")
    options.release = 17
    finalizedBy("spotbugsTest")
})

dependencies {
    compileOnly("org.springframework:spring-test:5.3.39")
    testImplementation("org.springframework:spring-test:5.3.39")
    testCompileOnly("org.jetbrains:annotations:26.0.2")
    implementation("com.jayway.jsonpath:json-path:2.9.0")
    compileOnly("org.jetbrains:annotations:26.0.2")
    testCompileOnly("org.jetbrains:annotations:26.0.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}