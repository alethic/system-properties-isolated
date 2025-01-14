allprojects {
    version = "1.0.0"
    group = "solutions.alethic"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java-library")
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "sonatype"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = project.findProperty("sonatype.user") as String? ?: System.getenv("sonatype.user")
                    password = project.findProperty("sonatype.password") as String? ?: System.getenv("sonatype.password")
                }

            }
        }
    }

    apply(plugin = "signing")    

    val test by tasks.getting(Test::class) {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces	= true
        }
    }

}