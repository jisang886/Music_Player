plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}


android {
    namespace = "com.example.music_zhanghongji"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.music_zhanghongji"
        minSdk = 30
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation("com.google.android.material:material:1.8.0")

    //下滑关闭
    implementation("me.imid.swipebacklayout.lib:library:1.1.0")

    //指示器库
    implementation("com.tbuonomo:dotsindicator:4.2")


    // Retrofit + Gson（网络请求与解析）
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Glide（图片加载）
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

// Kotlin 协程（网络异步）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Lifecycle（用于 lifecycleScope）
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// ViewPager2（banner横滑）
    implementation("androidx.viewpager2:viewpager2:1.0.0")

// RecyclerView（多样式展示）
    implementation("androidx.recyclerview:recyclerview:1.3.2")

// SwipeRefreshLayout（下拉刷新）
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    implementation("androidx.palette:palette:1.0.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}