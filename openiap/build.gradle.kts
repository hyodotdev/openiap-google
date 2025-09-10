plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

// Resolve version from either 'openIapVersion' or 'OPENIAP_VERSION' or fallback
val openIapVersion: String =
    (project.findProperty("openIapVersion")
        ?: project.findProperty("OPENIAP_VERSION")
        ?: "1.0.0").toString()

android {
    namespace = "dev.hyo.openiap"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    // Enable Compose for composables in this library (IapContext)
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion =
            (project.findProperty("COMPOSE_COMPILER_VERSION") as String?) ?: "1.5.14"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Google Play Billing Library
    api("com.android.billingclient:billing-ktx:6.0.1")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // JSON handling
    implementation("com.google.code.gson:gson:2.10.1")

    // Compose runtime (for CompositionLocal provider in IapContext)
    val composeUiVersion = (project.findProperty("COMPOSE_UI_VERSION") as String?) ?: "1.6.8"
    implementation("androidx.compose.runtime:runtime:$composeUiVersion")
    implementation("androidx.compose.ui:ui:$composeUiVersion")
    
    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.findProperty("OPENIAP_GROUP_ID")?.toString() ?: "dev.hyo.openiap"
            artifactId = "openiap-google"
            version = openIapVersion

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("OpenIAP GMS")
                description.set("OpenIAP Android library using Google Play Billing v8")
                url.set("https://github.com/hyodotdev/openiap-google")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("hyodo-dev")
                        name.set("hyodo.dev")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/hyodotdev/openiap-google.git")
                    developerConnection.set("scm:git:ssh://git@github.com/hyodotdev/openiap-google.git")
                    url.set("https://github.com/hyodotdev/openiap-google")
                }
            }
        }
    }
}
