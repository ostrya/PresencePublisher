buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.2")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.33.0"
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
