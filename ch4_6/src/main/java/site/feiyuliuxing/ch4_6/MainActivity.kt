package site.feiyuliuxing.ch4_6

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rendererIndex = intent.getIntExtra(EXTRA_RENDERER_INDEX, 0)
        setContentView(MyGLSurfaceView(this, rendererIndex))
    }

    companion object {
        private const val EXTRA_RENDERER_INDEX = "EXTRA_RENDERER_INDEX"

        fun startMe(context: Context, rendererIndex: Int) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(EXTRA_RENDERER_INDEX, rendererIndex)
            context.startActivity(intent)
        }
    }
}