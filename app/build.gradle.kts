import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import sp.kx.gradlex.buildDir
import sp.kx.gradlex.camelCase
import sp.kx.gradlex.create
import sp.kx.gradlex.map
import sp.kx.gradlex.qn
import sp.kx.gradlex.string
import sp.kx.gradlex.xml

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://central.sonatype.com/repository/maven-snapshots") // todo
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.compose") version Version.compose
}

android {
    namespace = "org.kepocnhh.hegel"
    compileSdk = Version.Android.compileSdk

    defaultConfig {
        applicationId = namespace
        minSdk = Version.Android.minSdk
        targetSdk = Version.Android.targetSdk
        versionCode = 1100
        versionName = "0.11.0"
        manifestPlaceholders["appName"] = "@string/app_name"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["buildType"] = name
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions.kotlinCompilerExtensionVersion = "1.5.15"

    productFlavors {
        "device".also { dimension ->
            flavorDimensions += dimension
            create("phone") {
                this.dimension = dimension
            }
            create("watch") {
                this.dimension = dimension
            }
        }
    }
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    output.outputFileName = listOf(
        rootProject.name,
        android.defaultConfig.versionName!!,
        variant.name,
        android.defaultConfig.versionCode!!.toString(),
    ).joinToString(separator = "-", postfix = ".apk")
    afterEvaluate {
        tasks.getByName<JavaCompile>(camelCase("compile", variant.name, "JavaWithJavac")) { // todo camelCase
            targetCompatibility = Version.jvmTarget
        }
        tasks.getByName<KotlinCompile>(camelCase("compile", variant.name, "Kotlin")) { // todo camelCase
            kotlinOptions.jvmTarget = Version.jvmTarget
        }
        val checkManifestTask = tasks.create("checkManifest", variant.name) {
            dependsOn(camelCase("compile", variant.name, "Sources"))
            doLast {
                val actual = buildDir()
                    .xml("intermediates/merged_manifest/${variant.name}/AndroidManifest.xml")
                    .map("uses-permission".qn()) {
                        it.string("{http://schemas.android.com/apk/res/android}name".qn())
                    }
                val applicationId by variant.applicationId
                val expected = setOf(
                    "android.permission.INTERNET",
                    "android.permission.FOREGROUND_SERVICE",
                    "android.permission.FOREGROUND_SERVICE_DATA_SYNC",
                    "android.permission.POST_NOTIFICATIONS",
                    "$applicationId.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                )
                check(actual.sorted() == expected.sorted()) {
                    "Actual is:\n$actual\nbut expected is:\n$expected"
                }
            }
        }
        tasks.getByName(camelCase("assemble", variant.name)) {
            dependsOn(checkManifestTask)
        }
    }
}

dependencies {
    debugImplementation("androidx.compose.ui:ui-tooling:${Version.compose}")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:${Version.compose}")
    debugImplementation("androidx.wear:wear-tooling-preview:1.0.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-service:2.8.6")
    implementation(compose.foundation)
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("com.github.kepocnhh:Bytes:0.4.0")
    implementation("com.github.kepocnhh:Secrets:0.2.0u-SNAPSHOT")
    implementation("com.github.kepocnhh:TLSMessages:0.1.0u-SNAPSHOT")
    implementation("com.github.kepocnhh:HttpReceiver:0.2.2u-SNAPSHOT")
    implementation("com.github.kepocnhh:BytesLoader:0.2.0u-SNAPSHOT")
    implementation("com.github.kepocnhh:Logics:0.1.3-SNAPSHOT")
    implementation("com.github.kepocnhh:Storages:0.10.0u-SNAPSHOT")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    "watchImplementation"("androidx.wear.compose:compose-foundation:1.3.1")
}
