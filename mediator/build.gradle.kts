plugins {
    id("common")
    application
}

val githubUser: String? by project
val githubPassword: String? by project

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/dp-kontrakter")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

dependencies {
    implementation(project(path = ":modell"))

    implementation(libs.dp.aktivitetslogg)
    // Kontrakter for dp-iverksett
    implementation("no.nav.dagpenger.kontrakter:iverksett:2.0_20231124154225_d640fba")

    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")

    // POC - iverksett api
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.jackson)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.dp.biblioteker.oauth2.klient)

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)

    implementation(libs.bundles.postgres)

    testImplementation(libs.mockk)
    testImplementation(libs.bundles.postgres.test)
    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")
}

application {
    mainClass.set("no.nav.dagpenger.vedtak.iverksett.AppKt")
}
