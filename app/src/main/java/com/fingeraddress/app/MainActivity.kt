package com.fingeraddress.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fingeraddress.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnroll.setOnClickListener {
            startActivity(Intent(this, EnrollActivity::class.java))
        }

        binding.btnVerify.setOnClickListener {
            startActivity(Intent(this, VerifyActivity::class.java))
        }
    }
}
