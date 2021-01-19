package com.example.happiens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.happiens.ui.history.HistoryFragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login_page.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginPage : AppCompatActivity() {
    //test
    var firebaseAuth: FirebaseAuth? = null
    var callbackManager: CallbackManager? = null
    var accessToken = AccessToken.getCurrentAccessToken()
    val database = FirebaseDatabase.getInstance()
    var myref = database.reference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        var facebookUserId = ""
        val user = firebaseAuth?.currentUser
        if (user != null) {
            for (profile in user.providerData) {
                if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                    facebookUserId = profile.uid
                }
            }
        }

        /*
        val photoUrl = "https://graph.facebook.com/$facebookUserId/picture?height=500"

        userFullName.text = user?.displayName
        userEmail.text = user?.email
        Picasso.with(this)
            .load(photoUrl)
            .into(userImage)

        login_button.setPermissions("email", "public_profile")
        login_button.setOnClickListener {
            signIn()
        }

         */

        fbLoginButtonCustom.setOnClickListener {
            newButtonSignin()
        }

    }

    

    fun newButtonSignin () {
        LoginManager.getInstance().logInWithReadPermissions(this,Arrays.asList("email","public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)



                /*        val user = firebaseAuth!!.currentUser
                        if (firebaseAuth!!.currentUser != null) {

                        }else{
                            myref.child("users").child(user!!.uid).setValue(user!!.email)
                        }

                 */


            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

        fun writeNewUser (email:String?){
        val authUsers = firebaseAuth!!.currentUser
        myref.child("users").child(authUsers!!.uid).setValue(email)
    }


    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnFailureListener { e ->
                updateUI(null)
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            .addOnSuccessListener { result ->
                val email = result.user!!.email
                val user = result.user!!.displayName
                val currentuser =    firebaseAuth!!.currentUser
                updateUI(currentuser)


                Toast.makeText(
                    this, " Welcome " + user, Toast.LENGTH_LONG).show()
                writeNewUser(email)
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    fun printKeyHash() {
        try {
            val info =
                packageManager.getPackageInfo("com.example.happiens", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }

        } catch (e: PackageManager.NameNotFoundException) {


        } catch (e: NoSuchAlgorithmException) {
        }

    }


}