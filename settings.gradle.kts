rootProject.name = "dp-vedtak-iverksett"
include("mediator")
include("modell")

dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("libs") {
            from("no.nav.dagpenger:dp-version-catalog:20231030.47.7ec442")
        }
    }
}
