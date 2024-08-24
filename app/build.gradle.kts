import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.filter.PathFilter

// license plugin needs google repo, see https://github.com/jaredsburrows/gradle-license-plugin/issues/129
buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
    }
}

plugins {
    id("com.github.triplet.play") version "3.10.1"
    id("com.jaredsburrows.license") version "0.9.8"
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
    return getBuildVersionName().matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+(?:-alpha[0-9]*|-beta[0-9]*)?"))
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "org.ostrya.presencepublisher"
        minSdk = 21
        multiDexEnabled = true
        targetSdk = 34
        vectorDrawables.useSupportLibrary = true
        versionCode = 52
        versionName = "2.5.3"
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
        abortOnError = false
    }
    namespace = "org.ostrya.presencepublisher"
    buildFeatures {
        buildConfig = true
    }
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

licenseReport {
    generateCsvReport = false
    generateHtmlReport = true
    generateJsonReport = false
    copyHtmlReportToAssets = true
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
                git.diff()
                    .setPathFilter(PathFilter.create("app/src/main/assets/open_source_licenses.html"))
                    .setOutputStream(System.out)
                    .setShowNameAndStatusOnly(false)
                    .call()
                throw InvalidUserDataException("License file has changed, please commit new version first")
            }
        }
    }
}

play {
    serviceAccountCredentials.set(file("../../google-key.json"))
    defaultToAppBundles.set(true)
}

dependencies {
    val roomVersion = "2.6.1"

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.room:room-guava:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.work:work-runtime:2.9.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.guava:guava:33.3.0-android")
    implementation("org.altbeacon:android-beacon-library:2.20.6")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}

tasks.register("printVersion") {
    doLast {
        println("Version code: ${getBuildVersionCode()}, version name: ${getBuildVersionName()}")
    }
}
