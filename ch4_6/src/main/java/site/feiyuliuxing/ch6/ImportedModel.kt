package site.feiyuliuxing.ch6

import site.feiyuliuxing.gl_comm.Vec2
import site.feiyuliuxing.gl_comm.Vec3
import java.io.InputStream
import java.util.Scanner

class ImportedModel(fileStream: InputStream) {
    private val numVertices: Int
    private val vertices = mutableListOf<Vec3>()
    private val texCoords = mutableListOf<Vec2>()
    private val normalVecs = mutableListOf<Vec3>()

    init {
        val modelImporter = ModelImporter()
        modelImporter.parseOBJ(fileStream)//先加载OBJ模型文件
        numVertices = modelImporter.getNumVertices()
        val verts = modelImporter.getVertices()
        val tcs = modelImporter.getTextureCoordinates()
        val normals = modelImporter.getNormals()

        for (i in 0 until numVertices) {
            vertices.add(Vec3(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2]))
            texCoords.add(Vec2(tcs[i * 2], tcs[i * 2 + 1]))
            normalVecs.add(Vec3(normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]))
        }
    }

    fun getNumVertices(): Int {
        return numVertices
    }

    fun getVertices(): List<Vec3> {
        return vertices
    }

    fun getTextureCoords(): List<Vec2> {
        return texCoords
    }

    fun getNormals(): List<Vec3> {
        return normalVecs
    }
}

class ModelImporter {
    private val vertVals = mutableListOf<Float>()
    private val triangleVerts = mutableListOf<Float>()
    private val textureCoords = mutableListOf<Float>()
    private val stVals = mutableListOf<Float>()
    private val normals = mutableListOf<Float>()
    private val normVals = mutableListOf<Float>()

    fun getNumVertices(): Int {
        return triangleVerts.size / 3
    }

    fun getVertices(): List<Float> {
        return triangleVerts
    }

    fun getNormals(): List<Float> {
        return normals
    }

    fun getTextureCoordinates(): List<Float> {
        return textureCoords
    }

    fun parseOBJ(fileStream: InputStream) {
        Scanner(fileStream).use { scanner ->
            var line: String
            while (scanner.hasNextLine()) {
                line = scanner.nextLine()
                if (line.startsWith("v ")) {
                    val xyz: List<Float> = line.substring(2)
                        .split(" ").map { it.toFloat() }
                    vertVals.addAll(xyz)
                } else if (line.startsWith("vt")) {
                    val st: List<Float> = line.substring(3)
                        .split(" ").map { it.toFloat() }
                    stVals.addAll(st)
                } else if (line.startsWith("vn")) {
                    val xyz: List<Float> = line.substring(3)
                        .split(" ").map { it.toFloat() }
                    normVals.addAll(xyz)
                } else if (line.startsWith("f")) {
                    // f 2/7/3 5/8/3 3/9/3
                    // 斜杠分隔的分别是 顶点列表/纹理坐标/法向量索引
                    val vtnList: List<String> = line.substring(2).split(" ")
                    for (s in vtnList) {
                        val vtn = s.split("/").map { it.toInt() }

                        val vertRef = (vtn[0] - 1) * 3
                        val tcRef = (vtn[1] - 1) * 2
                        val normRef = (vtn[2] - 1) * 3

                        triangleVerts.add(vertVals[vertRef])
                        triangleVerts.add(vertVals[vertRef + 1])
                        triangleVerts.add(vertVals[vertRef + 2])

                        textureCoords.add(stVals[tcRef])
                        textureCoords.add(stVals[tcRef + 1])

                        normals.add(normVals[normRef])
                        normals.add(normVals[normRef + 1])
                        normals.add(normVals[normRef + 2])
                    }
                }
            } // end of while loop
        }
    } //end of parseOBJ()

}