import org.gradle.api.Project

abstract class RedSmokesModuleExtension(private val project: Project) {
    val archiveFile = project.objects.fileProperty()
}