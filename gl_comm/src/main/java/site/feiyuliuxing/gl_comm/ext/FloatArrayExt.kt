package site.feiyuliuxing.gl_comm.ext

import java.nio.FloatBuffer

object FloatArrayExt {
    fun FloatArray.toBuffer(): FloatBuffer {
        return FloatBuffer.wrap(this)
    }

    fun List<Float>.toBuffer(): FloatBuffer {
        return toFloatArray().toBuffer()
    }
}