package com.marco.assignmenttwo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
//    AssetManager am

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonToThreadActivity.setOnClickListener {
            val intent = Intent(this, ThreadActivity::class.java)
            startActivity(intent)
        }

        buttonToAsyncActivity.setOnClickListener {
            val intent = Intent(this, AsyncActivity::class.java)
            startActivity(intent)
        }

        buttonToReadHomer.setOnClickListener {
            val intent = Intent(this, HomerReadActivity::class.java)
            startActivity(intent)
        }

        buttonToWriteHomer.setOnClickListener {
            val intent = Intent(this, HomerWriteActivity::class.java)
            startActivity(intent)
        }

    }
}
