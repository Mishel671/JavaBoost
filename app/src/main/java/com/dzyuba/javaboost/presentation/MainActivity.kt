package com.dzyuba.javaboost.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SplashFragment.newInstance())
                .commit()
        }
    }
}