package site.feiyuliuxing.gl_comm

interface IShaderProvider {
    fun vertexShaderSource(): String
    fun fragmentShaderSource(): String
}