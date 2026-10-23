plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.insane.goldtv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.insane.goldtv"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.2"
    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}


// Copy the user's uploaded artwork into Android resources before resource merging.
val prepareAppIcon by tasks.registering(Copy::class) {
    from("src/main/Screenshot_20261023_202055.jpg")
    into("src/main/res/drawable-nodpi")
    rename { "app_icon.jpg" }
}

tasks.named("preBuild").configure {
    dependsOn(prepareAppIcon)
}
