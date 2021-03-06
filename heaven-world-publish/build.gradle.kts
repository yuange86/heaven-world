//new credential processing to keep private
val credentialFile = File(projectDir.parentFile, "private.properties")

plugins {
    `maven-publish`
    signing
}

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "debug"
            url = rootProject.uri(".debug-server/libraries")
        }

        maven {
            name = "central"

            credentials.runCatching {
//                val nexusUsername: String by project
//                val nexusPassword: String by project
//                username = nexusUsername
//                password = nexusPassword

                val nexusUsername = credentialFile.readLines()[0]
                val nexusPassword = credentialFile.readLines()[1]

                logger.info("Username : $nexusUsername")
                logger.info("Password : $nexusPassword")

                username = nexusUsername
                password = nexusPassword

            }.onFailure {
                logger.warn("Failed to load nexus credentials, Check the gradle.properties")
            }

            url = uri(
                if ("SNAPSHOT" in version as String) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                }
            )
        }
    }

    publications {
        fun MavenPublication.setup(target: Project) {
            from(target.components["java"])
            artifact(target.tasks["sourcesJar"])
            artifact(target.tasks["dokkaJar"])

            pom {
                name.set(target.name)
                url.set("https://github.com/yuange86/${rootProject.name}")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("yuange86")
                        name.set("yuange")
                        email.set("xvdjk0806@gmail.com")
                        url.set("https://github.com/yuange86")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/yuange86/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:yuange86/${rootProject.name}.git")
                    url.set("https://github.com/yuange86/${rootProject.name}")
                }
            }
        }

        create<MavenPublication>("api") {
            val api = api
            artifactId = api.name
            setup(api)
        }

        create<MavenPublication>("core") {
            val core = core
            val dongle = dongle

            artifactId = core.name
            setup(core)

            core.tasks.jar { archiveClassifier.set("origin") }
            dongle.tasks {
                create<Jar>("publishJar") {
                    archiveBaseName.set(artifactId)

                    from(core.sourceSets["main"].output)
                    dependsOn(jar)
                    from(zipTree(jar.get().archiveFile))

                    artifact(this)
                }
            }
        }
    }
}

signing {
    isRequired = true
    sign(publishing.publications)
}
