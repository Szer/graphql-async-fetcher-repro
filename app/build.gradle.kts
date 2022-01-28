plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    implementation("com.expediagroup:graphql-kotlin-server:5.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
}

application {
    mainClass.set("graphql.async.fetcher.repro.AppKt")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                languageSettings.optIn("kotlin.RequiresOptIn")
            }
        }
    }
}
