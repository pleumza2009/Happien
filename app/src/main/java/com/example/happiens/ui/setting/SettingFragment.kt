package com.example.happiens.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.happiens.LoginPage
import com.example.happiens.MainActivity
import com.example.happiens.R
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : Fragment() {

    private lateinit var settingViewModel: SettingViewModel



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mAuth = FirebaseDatabase.getInstance()

        FacebookLogin.setOnClickListener {


            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("LOG OUT")
            builder.setMessage("Do you really want to log out ?")
            builder.setPositiveButton("YES"){ _, _ ->
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                val intent = Intent(activity, LoginPage::class.java)
                startActivity(intent)
                activity!!.finish()
            }
            builder.setNegativeButton("No"){ _, _ ->

            }
            val dialog: AlertDialog = builder.create()
            dialog.show()



        }
    }
}