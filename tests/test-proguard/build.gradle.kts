apply plugin: 'java-library'

// Note: use release flag instead of sourceCompatibility and targetCompatibility to ensure only JDK 8 API is used.
// https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_cross_compilation
tasks.withType(JavaCompile).configureEach {
    options.release.set(8)
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
    implementation project(':objectbox-java-api')

    // Check flag to use locally compiled version to avoid dependency cycles
    if (!project.hasProperty('noObjectBoxTestDepencies') || !noObjectBoxTestDepencies) {
        println "Using $obxJniLibVersion"
        implementation obxJniLibVersion
    } else {
        println "Did NOT add native dependency"
    }

    testImplementation "junit:junit:$junitVersion"
}
