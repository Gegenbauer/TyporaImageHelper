private val mdImageRefRegex = Regex("!\\[[^]]*]\\(([^)]*)\\)")

/**
 * str = "| 原型 demo | `Ctrl + Alt + Shift + Insert` | Scratch files<br />创建草稿文件，不会生成到项目中，而是作为一个临时文件，可以用来测试代码<br />![image-20230219183758987](./image-20230219183758987.png)<br />![image-20230219183823546](./image-20230219183823546.png) |"
 * use regex to extract all the image reference in str, eg: "./image-20230219183758987.png"
 */
fun extractImageRef(str: String): List<String> {
    val matchResults = mdImageRefRegex.findAll(str)
    val result = mutableListOf<String>()
    matchResults.forEach {
        result.add(it.groupValues[1])
    }
    return result
}

fun main() {
    val str = "| 原型 demo | `Ctrl + Alt + Shift + Insert` | Scratch files<br />创建草稿文件，不会生成到项目中，而是作为一个临时文件，可以用来测试代码<br />![image-20230219183758987](./image-20230219183758987.png)<br />![image-20230219183823546](./image-20230219183823546.png) |"
    println(extractImageRef(str))
}