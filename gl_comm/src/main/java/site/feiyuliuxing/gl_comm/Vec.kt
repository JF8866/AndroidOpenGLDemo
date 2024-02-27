package site.feiyuliuxing.gl_comm

data class Vec2 @JvmOverloads constructor(val s: Float = 0f, val t: Float = 0f)
data class Vec3 @JvmOverloads constructor(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) {

    /**
     * 求向量叉积
     */
    fun cross(other: Vec3): Vec3 {
        return Vec3(
            x = y * other.z - z * other.y,
            y = z * other.x - x * other.z,
            z = x * other.y - y * other.x
        )
    }

    //重载加号(+)运算法
    operator fun plus(other: Vec3): Vec3 {
        return Vec3(
            x = x + other.x,
            y = y + other.y,
            z = z + other.z
        )
    }
}