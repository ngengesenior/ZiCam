pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        /*maven {
            url = uri("https://androidx.dev/snapshots/builds/10250649/artifacts/repository")
        }*/

    }
}
dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "ZiCam"
include(":app")

