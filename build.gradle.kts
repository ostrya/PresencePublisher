buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.38.0"
}

allprojects {
    repositories {
        google()
        jcenter()
    }

    tasks.withType<JavaCompile> {
        options.isDeprecation = true
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
