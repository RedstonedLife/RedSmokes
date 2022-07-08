plugins {
    id("redsmokes.module-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    shadowJar {
        archiveClassifier.set(null)
    }
}

extensions.configure<RedSmokesModuleExtension> {
    archiveFile.set(tasks.shadowJar.flatMap { it.archiveFile })
}