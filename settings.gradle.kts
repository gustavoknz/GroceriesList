rootProject.name = "GroceriesList"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":core")
include(":server")

if (file("app/shared").exists()) {
    include(":app:shared")
}
if (file("app/webApp").exists()) {
    include(":app:webApp")
}
if (file("app/androidApp").exists()) {
    include(":app:androidApp")
}