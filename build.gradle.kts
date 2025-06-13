import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val theVersion = "1.0.0"
val theMainClass = "me.theentropyshard.sheet.MainKt"

group = "me.theentropyshard"
version = theVersion

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

tasks.create("createDebugZip", Zip::class) {
    dependsOn("createDistributable")
    from(projectDir.resolve("build/compose/binaries/main/app"))
    archiveBaseName = "Sheet"
    archiveVersion = theVersion
}

tasks.create("printVersion") {
    print("$theVersion\n")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
}

compose.desktop {
    application {
        mainClass = theMainClass

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("java.management")
            packageName = "Sheet"
            packageVersion = theVersion
        }
    }
}