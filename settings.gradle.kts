pluginManagement {
    repositories {
        google()          // ✅ gọi method, không phải block
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()        }
}



rootProject.name = "LKMS"
include(":app")
