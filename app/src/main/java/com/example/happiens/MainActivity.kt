package com.example.happiens

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
        var mAuth : FirebaseAuth? =null
        lateinit var sharedPreferences: SharedPreferences
        private val themeKey = "currentTheme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*theme.applyStyle(R.style.AppThemeOrange, true)*/
        sharedPreferences = getSharedPreferences(
            "ThemePref",
            Context.MODE_PRIVATE
        )

//        getTheme()applyStyle(R.style.OverlayThemeLime, true) in Java
//        theme.applyStyle(R.style.OverlayThemeBlue, true) // -> Replaced
        when (sharedPreferences.getString(themeKey, "green")) {
            "orange" ->  theme.applyStyle(R.style.AppThemeOrange, true)
            "red" ->  theme.applyStyle(R.style.AppThemeRed, true)
            "green" ->  theme.applyStyle(R.style.AppTheme, true)
            "blue" ->  theme.applyStyle(R.style.AppThemeBlue, true)
        }
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser !=null)
            else{
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_history,
            R.id.navigation_result,
            R.id.navigation_record,
            R.id.navigation_recommendation,
            R.id.navigation_setting
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        floatingButton.setOnClickListener {
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

    }
    fun onClick(view: View) {
        when(view.id) {
            R.id.orange -> {
                sharedPreferences.edit().putString(themeKey, "orange").apply()
            }

            R.id.red -> {
                sharedPreferences.edit().putString(themeKey, "red").apply()
            }

            R.id.green-> {
                sharedPreferences.edit().putString(themeKey, "green").apply()
            }

            R.id.blue -> {
                sharedPreferences.edit().putString(themeKey, "blue").apply()
            }

        }

        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        finish()
        startActivity(intent)

//        recreate()
    }
}


