apply plugin: 'java-library'
apply plugin: 'kotlin'

tasks.withType(JavaCompile).configureEach {
    // Note: use release flag instead of sourceCompatibility and targetCompatibility to ensure only JDK 8 API is used.
    // https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_cross_compilation
    options.release.set(8)
    // Note: Gradle defaults to the platform default encoding, make sure to always use UTF-8 for UTF-8 tests.
    options.encoding = "UTF-8"
}

// Produce Java 8 byte code, would default to Java 6.
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    // Native lib might be deployed only in internal repo
    if (project.hasProperty('gitlabUrl')) {
        println "gitlabUrl=$gitlabUrl added to repositories."
        maven {
            url "$gitlabUrl/api/v4/groups/objectbox/-/packages/maven"
            name "GitLab"
            credentials(HttpHeaderCredentials) {
                name = project.hasProperty("gitlabTokenName") ? gitlabTokenName : "Private-Token"
                value = gitlabPrivateToken
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        }
    } else {
        println "Property gitlabUrl not set."
    }
}

dependencies {
    implementation project(':objectbox-java')
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
    implementation project(':objectbox-kotlin')
    implementation "org.greenrobot:essentials:$essentialsVersion"

    // Check flag to use locally compiled version to avoid dependency cycles
    if (!project.hasProperty('noObjectBoxTestDepencies') || !noObjectBoxTestDepencies) {
        println "Using $obxJniLibVersion"
        implementation obxJniLibVersion
    } else {
        println "Did NOT add native dependency"
    }

    testImplementation "junit:junit:$junitVersion"
    // To test Coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    // To test Kotlin Flow
    testImplementation 'app.cash.turbine:turbine:0.5.2'
}

test {
    if (System.getenv("TEST_WITH_JAVA_X86") == "true") {
        // To run tests with 32-bit ObjectBox
        // Note: 32-bit JDK is only available on Windows
        def javaExecutablePath = System.getenv("JAVA_HOME_X86") + "\\bin\\java.exe"
        println("Will run tests with $javaExecutablePath")
        executable = javaExecutablePath
    } else if (System.getenv("TEST_JDK") != null) {
        // To run tests on a different JDK, uses Gradle toolchains API (https://docs.gradle.org/current/userguide/toolchains.html)
        def sdkVersionInt = System.getenv("TEST_JDK") as Integer
        println("Will run tests with JDK $sdkVersionInt")
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(sdkVersionInt))
        })
    }

    // This is pretty useless now because it floods console with warnings about internal Java classes
    // However we might check from time to time, also with Java 9.
    // jvmArgs '-Xcheck:jni'

    filter {
        // Note: Tree API currently incubating on Linux only.
        if (!System.getProperty("os.name").toLowerCase().contains('linux')) {
            excludeTestsMatching "io.objectbox.tree.*"
        }
    }

    testLogging {
        showStandardStreams = true
        exceptionFormat = 'full'
        displayGranularity = 2
        events 'started', 'passed'
    }
}