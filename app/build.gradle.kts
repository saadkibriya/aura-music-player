plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace   = "com.kibriya.aura"
    compileSdk  = 35

    defaultConfig {
        applicationId = "com.kibriya.aura"
        minSdk        = 31
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental",    "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled    = true
            isShrinkResources  = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix   = "-debug"
            isDebuggable        = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ── Compose BOM ─────────────────────────────────────────
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // ── Hilt ────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)          // was: libs.hilt.compiler
    implementation(libs.hilt.navigation.compose)

    // ── Room ────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── Media3 ──────────────────────────────────────────────
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)

    // ── Coil ────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Palette ─────────────────────────────────────────────
    implementation(libs.palette.ktx)         // was: libs.palette

    // ── WorkManager + Hilt-Work ─────────────────────────────
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)         // needed for hilt-work annotation processing

    // ── Navigation ──────────────────────────────────────────
    implementation(libs.navigation.compose)

    // ── Coroutines ──────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)   // was: libs.coroutines.core + libs.coroutines.android
                                                       // Note: core is a transitive dep of android variant

    // ── Splash Screen ───────────────────────────────────────
    implementation(libs.core.splashscreen)   // was: libs.splash.screen

    // ── Lifecycle ────────────────────────────────────────────
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
}