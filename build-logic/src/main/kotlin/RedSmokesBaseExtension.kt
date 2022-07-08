import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

abstract class RedSmokesBaseExtension(private val project: Project) {
    val injectBukkitApi: Property<Boolean> = project.objects.property<Boolean>().convention(true)
    val injectBstats: Property<Boolean> = project.objects.property<Boolean>().convention(true)
}