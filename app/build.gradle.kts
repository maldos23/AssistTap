plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    jacoco
}

android {
    namespace = "com.example.assisttap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.assisttap"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing comes from CI secrets (see docs/RELEASING.md); without them the release APK is unsigned.
    val keystoreFile = System.getenv("KEYSTORE_FILE")
    if (keystoreFile != null) {
        signingConfigs.create("release") {
            storeFile = file(keystoreFile)
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        // Version bumps are handled by Dependabot; targetSdk 35 is a project requirement.
        disable += setOf("OldTargetApi", "GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
        // aapt2 needs the adaptive icon in mipmap-anydpi-v26 even with minSdk 26.
        disable += "ObsoleteSdkInt"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ktlint {
    version.set(libs.versions.ktlint)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    // Robolectric runs on JUnit 4; the vintage engine runs those tests on the JUnit Platform.
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        // Robolectric loads classes through its own class loader; count them too.
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// Coverage gate: >= 80 % line coverage on the pure decision logic.
tasks.register<JacocoCoverageVerification>("verifyResolverCoverage") {
    group = "verification"
    description = "Fails when AssistantIntentResolver/IntentCandidates/AssistantToggle line coverage is below 80 %."
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            include(
                "**/AssistantIntentResolver*.class",
                "**/IntentCandidates*.class",
                "**/AssistantToggle*.class",
            )
        },
    )
    sourceDirectories.setFrom("src/main/java")
    executionData.setFrom(
        layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"),
    )
    // Never pass vacuously: Gradle skips this task without execution data, and an empty class set has 0/0 lines.
    setOnlyIf { true }
    doFirst {
        check(!classDirectories.asFileTree.isEmpty) { "No classes found for the coverage gate; check the class path." }
    }
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") { dependsOn("verifyResolverCoverage") }
