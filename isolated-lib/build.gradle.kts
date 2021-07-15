plugins {
    `java-library`
}

dependencies {
    testImplementation(enforcedPlatform("org.junit:junit-bom:5.5.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core:3.14.0")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("sonatype") {
            artifactId = "system-properties-isolated"
            from(components["java"])

            pom {
                name.set("Isolated System Properties")
                url.set("https://github.com/alethic/system-properties-isolated")
                description.set("Easy to use, lock-free, thread-safe isolated system properties to isolate changes to global state.")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("http://www.apache.org/licenses/")
                    }
                }
                developers {
                    developer {
                        id.set("alethic")
                        name.set("Jerome Haltom")
                        email.set("jhaltom@alethic.solutions")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:alethic/system-properties-isolated.git")
                    url.set("https://github.com/alethic/system-properties-isolated")
                }
                issueManagement {
                    url.set("https://github.com/alethic/system-properties-isolated/issues")
                    system.set("GitHub")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["sonatype"])
}