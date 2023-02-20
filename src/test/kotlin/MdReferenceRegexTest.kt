import com.gegenbauer.typora.imagehelper.MigrationRule
import kotlin.test.Test
import kotlin.test.junit5.JUnit5Asserter.assertEquals

class MigrationRuleTest {

    /**
     * str = "| 原型 demo | `Ctrl + Alt + Shift + Insert` | Scratch files<br />创建草稿文件，不会生成到项目中，而是作为一个临时文件，可以用来测试代码<br />![image-20230219183758987](./image-20230219183758987.png)<br />![image-20230219183823546](./image-20230219183823546.png) |"
     * use regex to extract all the image reference in str, eg: "./image-20230219183758987.png"
     */
    private fun extractImageRef(str: String): List<String> {
        val matchResults = MigrationRule.mdImageRefRegex.findAll(str)
        val result = mutableListOf<String>()
        matchResults.forEach {
            result.add(it.groupValues[1])
        }
        return result
    }

    @Test
    fun `should return correct image refs in line of markdown table`() {
        val str =
            "| 原型 demo | `Ctrl + Alt + Shift + Insert` | Scratch files<br />创建草稿文件，不会生成到项目中，而是作为一个临时文件，可以用来测试代码<br />![image-20230219183758987](./image-20230219183758987.png)<br />![image-20230219183823546](./image-20230219183823546.png) |"
        val result = extractImageRef(str)
        assertEquals("mdImageRefRegex", 2, result.size)
        assertEquals("mdImageRefRegex", "./image-20230219183758987.png", result[0])
        assertEquals("mdImageRefRegex", "./image-20230219183823546.png", result[1])
    }
}