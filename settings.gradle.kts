pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ClimateSense"
include(":app")
