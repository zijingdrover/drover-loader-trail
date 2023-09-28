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
        maven {url=uri("https://s01.oss.sonatype.org/content/groups/public")}
        maven {url=uri("https://s01.oss.sonatype.org/content/groups/staging")}
        google()
        mavenCentral()
    }
}

rootProject.name = "loader"
include(":app")
 