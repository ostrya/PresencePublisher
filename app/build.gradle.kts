import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk

// license plugin needs google repo, see https://github.com/jaredsburrows/gradle-license-plugin/issues/129
buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
    }
}

plugins {
    id("com.github.triplet.play") version "3.0.0"
    id("com.jaredsburrows.license") version "0.8.80"
    id("com.android.application")
}

val git: Git = Git.open(rootDir)

fun getBuildVersionCode(): Int {
    val headWalk: RevWalk = git.log().call() as RevWalk
    val head = headWalk.next()
    val tagList = git.tagList().call()
    val tagCount = tagList.count {
        it.name.matches(Regex("refs/tags/v[1-9].*"))
                && headWalk.isMergedInto(headWalk.parseCommit(it.objectId), head)
    }
    return if (isTagged()) tagCount else tagCount + 1
}

fun getBuildVersionName(): String {
    return git.describe().setTags(true).setMatch("v[1-9].*").call().substring(1)
}

fun isTagged(): Boolean {
    return getBuildVersionName().matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "org.ostrya.presencepublisher"
        minSdkVersion(14)
        targetSdkVersion(30)
        versionCode = 38
        versionName = "2.2.7"
    }
    signingConfigs {
        register("release") {
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
        named("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
        }
        named("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")?.takeIf { it.isSigningReady }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion("29.0.3")
}

val checkParameters by tasks.registering {
    doLast {
        if (android.defaultConfig.versionCode != getBuildVersionCode()) {
            throw InvalidUserDataException("Version code should be ${getBuildVersionCode()}")
        }
        if (isTagged() && android.defaultConfig.versionName != getBuildVersionName()) {
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
            if (git.status().addPath("app/src/main/assets/open_source_licenses.html").call().hasUncommittedChanges()) {
                throw InvalidUserDataException("License file has changed, please commit new version first")
            }
        }
    }
}

play {
    serviceAccountCredentials.set(file("../../google-key.json"))
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.3.0-alpha02")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha03")
    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    implementation("com.google.android.material:material:1.3.0-beta01")
    implementation("com.hypertrack:hyperlog:0.0.10") {
        exclude(group = "com.android.volley")
    }
    implementation("org.altbeacon:android-beacon-library:2.17.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("org.mockito:mockito-inline:3.6.28")
    testRuntimeOnly("com.android.volley:volley:1.1.1")
}

tasks.register("printVersion") {
    doLast {
        println("Version code: ${getBuildVersionCode()}, version name: ${getBuildVersionName()}")
    }
}
