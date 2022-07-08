import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.publishing")
}

val baseExtension = extensions.create<RedSmokesBaseExtention("redsmokes", project)

val checkstyleVersion = "8.36.2"
val spigotVersion = "1.19-R0.1-SNAPSHOT"
val junit5Version = "5.7.0"
val mockitoVersion = "3.2.0"

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter", junit5Version)
    testImplementation("org.junit.vintage", "junit-vintage-engine", junit5Version)
    testImplementation("org.mockito", "mockito-core", mockitoVersion)

    constraints {
        implementation("org.yaml:snakeyaml:1.28") {
            because("Bukkit API ships old versions, Configurate requires modern versions")
        }
    }
}