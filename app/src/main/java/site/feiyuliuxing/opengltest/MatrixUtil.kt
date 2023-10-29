package site.feiyuliuxing.opengltest

object MatrixUtil {

    /**
     * 创建个单位矩阵
     */
    fun mat4(v: Float): FloatArray {
        val arr = FloatArray(16)
        for (i in 0 until 4) {
            arr[i * 4 + i] = v
        }
        return arr
    }

    fun printMat4(arr: FloatArray) {
        println("+".repeat(30))
        val sb = mutableListOf<Float>()
        for (i in 0 until 4) {
            sb.clear()
            for (j in 0 until 4) {
                sb.add(arr[i * 4 + j])
            }
            println(sb.joinToString(", "))
        }
        println("-".repeat(30))
    }
}