import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.gms)
    alias(libs.plugins.sonar)
    id("jacoco")
}

android {
    namespace = "com.swent.suddenbump"
    compileSdk = 34

    // Load the API key from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY") ?: ""

    defaultConfig {
        applicationId = "com.swent.suddenbump"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    testCoverage {
        jacocoVersion = "0.8.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Updated sourceSets configuration
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            kotlin.srcDirs("src/main/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java", "src/main/java")
            kotlin.srcDirs("src/androidTest/kotlin", "src/main/kotlin")
            res.srcDirs("src/main/res")
            manifest.srcFile("src/main/AndroidManifest.xml")
        }

        // Commenting out the custom sourceSets manipulation
        /*
        val testDebug by getting {
            val test by getting

            java.setSrcDirs(test.java.srcDirs)
            res.setSrcDirs(test.res.srcDirs)
            resources.setSrcDirs(test.resources.srcDirs)
        }

        val test by getting {
            java.setSrcDirs(emptyList<File>())
            res.setSrcDirs(emptyList<File>())
            resources.setSrcDirs(emptyList<File>())
        }
        */
    }
}

sonar {
    properties {
        property("sonar.projectName", "Sudden-Bump")
        property("sonar.projectKey", "swent-group4_sudden-bump")
        property("sonar.organization", "swent-group4")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

dependencies {
    // Firebase BoM
    implementation(platform(libs.firebase.bom))
    testImplementation(platform(libs.firebase.bom))
    androidTestImplementation(platform(libs.firebase.bom))

    // Firebase dependencies with protobuf-lite excluded
    implementation(libs.firebase.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    testImplementation(libs.firebase.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.firebase.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }

    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.ui.auth)

    // Explicitly include protobuf-javalite
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
    testImplementation("com.google.protobuf:protobuf-javalite:3.21.12")
    androidTestImplementation("com.google.protobuf:protobuf-javalite:3.21.12")

    // Exclude protobuf-lite globally in test configurations
    configurations {
        named("testImplementation") {
            exclude(group = "com.google.protobuf", module = "protobuf-lite")
        }
        named("androidTestImplementation") {
            exclude(group = "com.google.protobuf", module = "protobuf-lite")
        }
    }

    // Other dependencies...
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.androidx.arch.core.testing)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.test.core.ktx)
    implementation(libs.androidx.lifecycle.common.jvm)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.espresso.intents)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(kotlin("test"))

    // Jetpack Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)

    // Include main dependencies in androidTestImplementation
    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.material)
    androidTestImplementation(libs.androidx.lifecycle.runtime.ktx)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.play.services.maps)
    androidTestImplementation(libs.play.services.location)
    androidTestImplementation(libs.compose.ui)
    androidTestImplementation(libs.compose.ui.graphics)
    androidTestImplementation(libs.compose.material3)
    androidTestImplementation(libs.compose.activity)
    androidTestImplementation(libs.compose.viewmodel)
    androidTestImplementation(libs.compose.preview)
    androidTestImplementation(libs.compose.tooling)
    androidTestImplementation(libs.compose.test.junit)
    androidTestImplementation(libs.compose.test.manifest)
    androidTestImplementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.androidx.navigation.fragment.ktx)
    androidTestImplementation(libs.androidx.navigation.ui.ktx)
    androidTestImplementation(libs.libphonenumber)
    androidTestImplementation(libs.ucrop)
    androidTestImplementation(libs.maps.compose)
    androidTestImplementation(libs.maps.compose.utils)
    androidTestImplementation(libs.play.services.auth)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(libs.coil.compose)
    androidTestImplementation(libs.coil.network.okhttp)

    // Testing libraries
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    testImplementation(libs.json)

    globalTestImplementation(libs.kaspresso)
    globalTestImplementation(libs.kaspresso.compose)
    testImplementation(libs.core.testing)

    // UI Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.kaspresso)
    testImplementation(libs.kaspresso.compose)
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.allure.support)
    testImplementation(libs.kotlinx.coroutines.test)
}

configurations.all {
    resolutionStrategy {
        // Force specific versions to avoid conflicts
        force("com.google.protobuf:protobuf-javalite:3.21.12")
        force("androidx.test.espresso:espresso-core:3.5.1")
    }
}

tasks.withType<Test> {
    // Configure Jacoco for each test
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/sigchecks/**",
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.layout.projectDirectory}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}
