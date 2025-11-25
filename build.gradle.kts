buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.1")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.53.0"
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
