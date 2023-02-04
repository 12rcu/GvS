rootProject.name = "gvs"
include("exercise1")
include("exercise2")
include("exercise3")
include("exercise4")

pluginManagement {
    repositories {
        maven { // The google mirror is less flaky than mavenCentral()
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        gradlePluginPortal()
    }
}
include("exercise5")
include("exercise6")
include("exercise7")
