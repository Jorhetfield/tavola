package com.tavola.resources

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Element

/**
 * Keeps the string resources of every module in order (see CLAUDE.md: strings in `values/` (es) and `values-en/` (en),
 * with a prefix for the screen or the component that owns them).
 *
 * A new module, or a new owner of strings inside one, has to be declared in [allowedPrefixes]: that is the moment to
 * decide its prefix, so that names never collide between modules and a name says where it is used.
 */
class StringResourcesConventionTest {
    /** Prefixes each module may use, without the trailing underscore: the screen or the component that owns the string. */
    private val allowedPrefixes = mapOf(
        "app" to setOf("app"),
        "core/designsystem" to setOf("topbar", "search_field", "snackbar", "nav", "meal_slot", "day_card", "task_chip", "week_selector"),
        "feature/auth" to setOf("auth"),
        "feature/calendar" to setOf("calendar"),
        "feature/groups" to setOf("group"),
        "feature/recipes" to setOf("recipe", "meal_picker", "ingredient_unit"),
    )

    private val root: File = generateSequence(File("").absoluteFile) { directory -> directory.parentFile }
        .first { directory -> File(directory, "settings.gradle.kts").exists() }

    private fun resourcesOf(module: String, folder: String): Map<String, Set<String>> {
        val file = File(root, "$module/src/main/res/$folder/strings.xml")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.documentElement.childNodes
        return (0 until nodes.length)
            .map { index -> nodes.item(index) }
            .filterIsInstance<Element>()
            .associate { element -> element.getAttribute("name") to placeholdersIn(element.textContent) }
    }

    private fun placeholdersIn(text: String): Set<String> = PLACEHOLDER.findAll(text).map { match -> match.value }.toSet()

    @Test
    fun `every module that has strings is declared with its prefixes`() {
        val modulesWithStrings = root.walkTopDown()
            .onEnter { directory -> directory.name != "build" && directory.name != ".git" && directory.name != ".gradle" }
            .filter { file ->
                file.name == "strings.xml" && file.parentFile.name == "values" &&
                    file.path.contains("src${File.separator}main")
            }
            .map { file -> file.relativeTo(root).invariantSeparatorsPath.substringBefore("/src/main/res") }
            .toSet()

        assertEquals(allowedPrefixes.keys, modulesWithStrings)
    }

    @Test
    fun `spanish and english define the same strings with the same placeholders`() {
        allowedPrefixes.keys.forEach { module ->
            val spanish = resourcesOf(module, "values")
            val english = resourcesOf(module, "values-en")

            assertEquals(spanish.keys, english.keys, "$module: names differ between values and values-en")
            spanish.forEach { (name, placeholders) ->
                assertEquals(placeholders, english.getValue(name), "$module: placeholders of $name differ between es and en")
            }
        }
    }

    @Test
    fun `names are lower snake case and start with a prefix of their module`() {
        allowedPrefixes.forEach { (module, prefixes) ->
            resourcesOf(module, "values").keys.forEach { name ->
                assertTrue(SNAKE_CASE.matches(name), "$module: $name is not lower snake case")
                assertTrue(
                    prefixes.any { prefix -> name == prefix || name.startsWith("${prefix}_") },
                    "$module: $name does not start with one of $prefixes",
                )
            }
        }
    }

    @Test
    fun `no name is used by two modules`() {
        val owners = allowedPrefixes.keys
            .flatMap { module -> resourcesOf(module, "values").keys.map { name -> name to module } }
            .groupBy({ pair -> pair.first }, { pair -> pair.second })

        val repeated = owners.filterValues { modules -> modules.size > 1 }
        assertTrue(repeated.isEmpty(), "Names defined in more than one module: $repeated")
    }

    private companion object {
        val SNAKE_CASE = Regex("[a-z][a-z0-9]*(_[a-z0-9]+)*")
        val PLACEHOLDER = Regex("%(\\d+\\$)?[sd]")
    }
}
