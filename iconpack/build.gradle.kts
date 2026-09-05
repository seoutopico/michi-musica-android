plugins { id("com.android.application") }

android {
    namespace = "com.ainalluna.michiicons"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.ainalluna.michiicons"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

apply(from = rootProject.file("gradle/release-signing.gradle"))
