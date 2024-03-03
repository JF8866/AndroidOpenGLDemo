package site.feiyuliuxing.txt

class GLText(text: String, glChars: Map<Char, GLChar>) {
    private val glCharList = mutableListOf<GLChar>()

    init {
        for (c in text) glChars[c]?.let(glCharList::add)
    }

    fun draw(vbo: IntArray, offsetBlock: (Float, Float)->Unit) {
        val textWidth = glCharList.sumOf { it.glWidth.toDouble() }.toFloat()
        var xOffset = -textWidth / 2f

        for (glChar in glCharList) {
            offsetBlock(xOffset, 0f)
            glChar.draw(vbo)
            xOffset += glChar.glWidth
        }
    }
}