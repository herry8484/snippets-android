// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("androidx.navigation.safeargs") version "2.7.7" apply false
    id("com.github.ben-manes.versions") version "0.51.0" apply true
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

fun isNonStable(candidate: ModuleComponentIdentifier): Boolean {
    return listOf("alpha", "beta", "rc", "snapshot", "-m", "final").any { keyword ->
        keyword in candidate.version.lowercase()
    }
}

fun isBlockListed(candidate: ModuleComponentIdentifier): Boolean {
    return listOf(
            "androidx.browser:browser",
            "com.facebook.android:facebook-android-sdk",
            "com.applovin:applovin-sdk",
            "com.ironsource.sdk:mediationsdk"
    ).any { keyword ->
        keyword in candidate.toString().lowercase()
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate) || isBlockListed(candidate)
    }
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.49.0") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

tasks.register<JavaExec>("ktlintCheck") {
    val outputDir = "${project.buildDir}/reports/ktlint/"
    val inputFiles = project.fileTree("src").include("**/*.kt")
    val outputFile = "${outputDir}ktlint-checkstyle-report.xml"

    // See: https://medium.com/@vanniktech/making-your-gradle-tasks-incremental-7f26e4ef09c3
    inputs.files(inputFiles)
    outputs.file(outputFile)

    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")

    args(
            "--format",
            "--code-style=android_studio",
            "--reporter=plain",
            "--reporter=checkstyle,output=${outputFile}",
            "**/*.kt"
    )

    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}
