plugins {
    id("java")
}

group = "de.matthias"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/jeromq-0.5.2.jar"))
    implementation("com.google.guava:guava:31.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}