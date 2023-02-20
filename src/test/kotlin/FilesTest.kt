import com.gegenbauer.typora.imagehelper.getRelativePath
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.junit5.JUnit5Asserter.assertEquals

class FilesTest {

    @Test
    fun `should return correct relative path`() {
        val file = File("/home/meizu/MigirationTest/note/android/framework/AppTransition/AppTransition.md")
        val targetFile = File("/home/meizu/MigirationTest/note/img/2020-03-02-16-03-05.png")
        val relativePath = file.getRelativePath(targetFile)
        assertEquals("getRelativePath", "../../../img/2020-03-02-16-03-05.png", relativePath)
    }
}