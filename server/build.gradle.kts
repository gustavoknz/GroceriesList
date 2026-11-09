plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
}

group = "com.kieling.groceries"
version = "1.0.0"
application {
    mainClass = "com.kieling.groceries.ApplicationKt"
}

dependencies {
    api(project(":core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.kotlin.testJunit)
}

if (findProject(":app:webApp") != null) {
    evaluationDependsOn(":app:webApp")
    val webAppProject = project(":app:webApp")
    tasks.named<ProcessResources>("processResources") {
        dependsOn(webAppProject.tasks.named("jsBrowserDistribution"))
        from(webAppProject.layout.buildDirectory.dir("dist/js/productionExecutable")) {
            into("static")
        }
    }
}

