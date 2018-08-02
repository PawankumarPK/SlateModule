package com.example.android_1.slatemodule

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.technocomsolutions.slate.activities.SlateActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextView.setOnClickListener {
            val intent = Intent(this,SlateActivity::class.java)
            startActivity(intent)
        }

    }
}


