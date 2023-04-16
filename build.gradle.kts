buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.46.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.isDeprecation = true
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
