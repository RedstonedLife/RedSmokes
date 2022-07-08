import org.gradle.api.Project

abstract class RedSmokesModulExtension(private val project: Project) {
    val archiveFile = project.objects.fileProperty()
}