import com.android.build.api.dsl.ApplicationExtension
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk

buildscript {
    dependencies {
        classpath(libs.jgit)
    }
}

plugins {
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.play)
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

extensions.configure<ApplicationExtension> {
    compileSdk = 37
    defaultConfig {
        applicationId = "org.ostrya.presencepublisher"
        minSdk = 23
        targetSdk = 37
        vectorDrawables.useSupportLibrary = true
        versionCode = 59
        versionName = "2.7.0"
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        register("release") {
            val keystorePath = project.findProperty("keystorePath") as String?
            val keystorePassword = project.findProperty("keystorePassword") as String?
            val signkeyAlias = project.findProperty("signkeyAlias") as String?
            val signkeyPassword = project.findProperty("signkeyPassword") as String?
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
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        // below SDK 33, desugarizing will take care of compatibility to Java 7 / 8
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        abortOnError = false
    }
    namespace = "org.ostrya.presencepublisher"
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

play {
    serviceAccountCredentials.set(file("../../google-key.json"))
    defaultToAppBundles.set(true)
}

dependencies {
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.appcompat)
    implementation(libs.beacon)
    implementation(libs.cardview)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.guava)
    implementation(libs.material)
    implementation(libs.mqtt)
    implementation(libs.preference)
    implementation(libs.room.guava)
    implementation(libs.room.runtime)
    implementation(libs.viewpager2)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.assertj.core)

    annotationProcessor(libs.room.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

val checkParameters = tasks.register("checkParameters") {
    description = "Verify build version against git tag"
    doLast {
        if (android.defaultConfig.versionCode != getBuildVersionCode()) {
            throw InvalidUserDataException("Version code should be ${getBuildVersionCode()}")
        }
        if (isTagged() && android.defaultConfig.versionName != getBuildVersionName()) {
            throw InvalidUserDataException("Version name should be ${getBuildVersionName()}")
        }
    }
}

val printVersion = tasks.register("printVersion") {
    dependsOn(checkParameters)
    description = "Print actual version values"
    doLast {
        println("Version code: ${getBuildVersionCode()}, version name: ${getBuildVersionName()}")
    }
}

tasks.named("preBuild") {
    dependsOn(printVersion)
}
