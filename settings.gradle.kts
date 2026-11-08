pluginManagement {
    repositories {
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

rootProject.name = "DualUnits"
// karoo-ext SDK is vendored from github.com/hammerheadnav/karoo-ext (v1.1.9, Apache-2.0)
// so no GitHub Packages token is needed to build.
include(":karoo-ext", ":app")
