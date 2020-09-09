// license plugin needs google repo, see https://github.com/jaredsburrows/gradle-license-plugin/issues/129
buildscript {
    repositories {
        google()
    }
}

plugins {
    id("com.github.triplet.play") version "2.8.0"
    id("com.jaredsburrows.license") version "0.8.80"
    id("org.ajoberstar.grgit") version "4.0.2"
    id("com.android.application")
}

fun getBuildVersionCode(): Int {
    // The IDE is not clever enough to understand the groovy style syntax grgit.tag.list() here ...
    val tagList = org.ajoberstar.grgit.operation.TagListOp(grgit.repository).call()
    val tagCount = tagList.count { it.name.matches(Regex("v[1-9].*")) }
    return if (isTagged()) tagCount else tagCount + 1
}

fun getBuildVersionName(): String {
    return grgit.describe(mapOf<String, Any>("tags" to true, "match" to arrayOf("v[1-9]*"))).substring(1)
}

fun isTagged(): Boolean {
    return getBuildVersionName().matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "org.ostrya.presencepublisher"
        minSdkVersion(14)
        targetSdkVersion(29)
        versionCode = 28
        versionName = "2.1.0"
    }
    signingConfigs {
        create("release") {
            val keystorePath: String? by project
            val keystorePassword: String? by project
            val signkeyAlias: String? by project
            val signkeyPassword: String? by project
            storeFile = keystorePath?.let { file(it) }
            storePassword = keystorePassword
            keyAlias = signkeyAlias
            keyPassword = signkeyPassword
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion("29.0.2")
}

val checkParameters by tasks.registering {
    doLast {
        if (android.defaultConfig.versionCode != getBuildVersionCode()) {
            throw InvalidUserDataException("Version code should be ${getBuildVersionCode()}")
        }
        if (isTagged() && getBuildVersionName() != android.defaultConfig.versionName) {
            throw InvalidUserDataException("Version name should be ${getBuildVersionName()}")
        }
    }
}

afterEvaluate {
    val licenseDebugReport by tasks.existing

    tasks.preBuild {
        dependsOn(checkParameters)
    }
    tasks.register("checkUpdatedLicenseFile") {
        dependsOn(licenseDebugReport)
        doLast {
            if (grgit.status().unstaged.modified.contains("app/src/main/assets/open_source_licenses.html")) {
                throw InvalidUserDataException("License file has changed, please commit new version first")
            }
        }
    }
}

play {
    serviceAccountCredentials = file("../../google-key.json")
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha02")
    implementation("com.hypertrack:hyperlog:0.0.10") {
        exclude(group = "com.android.support")
        exclude(group = "com.android.volley")
    }
    implementation("org.altbeacon:android-beacon-library:2.17.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
}
