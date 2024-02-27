package site.feiyuliuxing.ch4_6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        listView.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, arrayOf(
                "4.6 渲染一个对象的多个副本", //0
                "4.7 渲染多个不同的模型", //1
                "4.8 矩阵栈", //2
                "5.7 纹理贴图", //3
                "6.1 程序构建模型---球体", //4
                "6.2 OpenGL索引---环面", //5
                "6.3 加载外部构建的模型", //6
            )
        )
        listView.setOnItemClickListener { adapterView, view, i, l ->
            MainActivity.startMe(this, i)
        }
        setContentView(listView)
    }
}