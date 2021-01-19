package com.example.happiens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.happiens.R
import com.example.happiens.dataclass.DateHelper
import com.example.happiens.dataclass.UserActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_page.*

class ActivityDetailPage : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    private var dbRef = database.reference
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.getReferenceFromUrl("gs://happiens-d1b51.appspot.com")

    private var activity: UserActivity = UserActivity("", "", "",
        0.0, "", "", "")
    lateinit var sharedPreferences: SharedPreferences
    private val themeKey = "currentTheme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(
            "ThemePref",
            Context.MODE_PRIVATE
        )
        when (sharedPreferences.getString(themeKey, "green")) {
            "orange" ->  theme.applyStyle(R.style.AppThemeOrange, true)
            "red" ->  theme.applyStyle(R.style.AppThemeRed, true)
            "green" ->  theme.applyStyle(R.style.AppTheme, true)
            "blue" ->  theme.applyStyle(R.style.AppThemeBlue, true)
        }
        setContentView(R.layout.activity_detail_page)

        val bundle: Bundle = intent.extras
        activity = UserActivity(bundle.getString("actId"), "", bundle.getString("actName"),
            bundle.getString("actScore").toDouble(), bundle.getString("actAltText"),
            bundle.getString("detailImage"), bundle.getString("actDate"))

        val actNameToSource = resources.getIdentifier(
            "com.example.happiens:drawable/${bundle.getString("actIcon")}",
            null,
            null
        )
        actDetailIcon.setImageResource(actNameToSource)
        actDetailName.text = activity.actName
        actDetailScore.text = activity.actScore.toString()
        actDetailAltText.text = activity.actAltText

        if(activity.actAltText == ""){
            actDetailAltText.visibility = View.GONE
        }

        actDetailDate.text = DateHelper.splitTimestamp(activity.timestamp)

        if(activity.actImg != ""){
            val gsReference = storage.getReferenceFromUrl(activity.actImg)
            storageRef.child(gsReference.path).downloadUrl.addOnSuccessListener {
                Picasso.with(this)
                    .load(it)
                    .into(actDetailImage)
            }.addOnFailureListener {

            }.addOnCompleteListener {
                progressBar.visibility = View.INVISIBLE
            }
        } else {
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editAct -> {
                val intent = Intent(this, EditPage::class.java)
                intent.putExtra("actId", activity.actId)
                intent.putExtra("actName", activity.actName)
                intent.putExtra("actScore", activity.actScore.toString())
                intent.putExtra("actAltText", activity.actAltText)
                intent.putExtra("actDate", activity.timestamp)
                intent.putExtra("detailImage", activity.actImg)
                this.startActivity(intent)
                finish()
                true
            }
            R.id.deleteAct -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete your activity")
                builder.setMessage("Do you want to delete this activity?")
                builder.setPositiveButton("YES"){ _, _ ->
                    dbRef.child("activities").child(activity.actId).removeValue()
                    if(activity.actImg != ""){
                        val gsReference = storage.getReferenceFromUrl(activity.actImg)
                        storageRef.child(gsReference.path).delete().addOnSuccessListener {

                        }.addOnFailureListener {

                        }
                    }
                    Toast.makeText(this,"Delete successfully.",Toast.LENGTH_SHORT).show()
                    finish()
                }
                builder.setNegativeButton("No"){ _, _ ->

                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
