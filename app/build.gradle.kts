plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.proyecto.scann"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.proyecto.scann"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation (libs.cardview)

    // Retrofit para realizar solicitudes HTTP(com.squareup.retrofit2:retrofit:2.9.0)
    implementation (libs.retrofit)
    // Gson para convertir objetos JSON(com.squareup.retrofit2:converter-gson:2.9.0)
    implementation (libs.converter.gson)
    // Logging Interceptor para depuración de solicitudes(com.squareup.okhttp3:logging-interceptor:4.9.3)
    implementation(libs.logging.interceptor)
    // ZXing para escaneo de códigos QR(com.journeyapps:zxing-android-embedded:4.3.0)
    implementation(libs.zxing.android.embedded.v430)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}