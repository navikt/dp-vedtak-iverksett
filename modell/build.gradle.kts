plugins {
    id("common")
}

dependencies {
    implementation(libs.dp.aktivitetslogg)
    implementation(libs.kotlin.logging)
    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")
}
