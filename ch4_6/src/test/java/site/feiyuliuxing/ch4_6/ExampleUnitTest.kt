package site.feiyuliuxing.ch4_6

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val s = "f 1/1/1 2/2/1 3/3/1"
        val iList = s.substring(2).split(" ")
            .map { vtn -> vtn.split("/").map { it.toInt() } }
        for (i in iList) {
            println(i)
        }
    }
}