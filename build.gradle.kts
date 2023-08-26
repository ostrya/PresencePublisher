buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.47.0"
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
