dependencies {
    api(projects.api)
    implementation(libs.yaml)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.group}.crate.shaded.${targetPkg}")
        reloc("org.yaml", "snakeyaml")

        minimize()
    }
}

deployer {
    release {
        version.set("${rootProject.version}")
        description.set(rootProject.description.orEmpty())
    }

    projectInfo {
        groupId = "io.github.milkdrinkers"
        artifactId = "crate-yaml"
        version = "${rootProject.version}"

        name = rootProject.name
        description = rootProject.description.orEmpty()
        url = "https://github.com/milkdrinkers/Crate"

        scm {
            connection = "scm:git:git://github.com/milkdrinkers/Crate.git"
            developerConnection = "scm:git:ssh://github.com:milkdrinkers/Crate.git"
            url = "https://github.com/milkdrinkers/Crate"
        }

        license({
            name = "GNU General Public License Version 3"
            url = "https://opensource.org/license/gpl-3-0/"
        })

        developer({
            name.set("darksaid98")
            email.set("darksaid9889@gmail.com")
            url.set("https://github.com/darksaid98")
            organization.set("Milkdrinkers")
        })
    }

    content {
        component {
            fromJava()
        }
    }

    centralPortalSpec {
        auth.user.set(secret("MAVEN_USERNAME"))
        auth.password.set(secret("MAVEN_PASSWORD"))
    }

    signing {
        key.set(secret("GPG_KEY"))
        password.set(secret("GPG_PASSWORD"))
    }
}