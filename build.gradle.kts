plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven(url = "https://repo.runelite.net")
    mavenCentral()
}

dependencies {
    implementation("com.displee:rs-cache-library:6.5")
    implementation("net.runelite:cache:latest.release")
    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    annotationProcessor("org.projectlombok:lombok:1.18.10")
    compileOnly("org.projectlombok:lombok:1.18.10")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}