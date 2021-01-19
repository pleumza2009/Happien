package com.example.happiens.ui.history

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.happiens.*
import com.example.happiens.dataclass.DateHelper
import com.example.happiens.dataclass.FileNameStringHelper

import com.example.happiens.dataclass.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.date_ticket.view.*
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_recommendation.view.*
import kotlinx.android.synthetic.main.mood_ticket.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HistoryFragment : Fragment() {

    private var database = FirebaseDatabase.getInstance()
    private var dbRef = database.reference
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.getReferenceFromUrl("gs://happiens-d1b51.appspot.com")
    var firebaseAuth:FirebaseAuth? =null
    var combineDate = ""

    var listOfActivities = ArrayList<UserActivity>()
    var adapter: ActivitiesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ActivitiesAdapter(activity, listOfActivities)
        actUserList.adapter = adapter

            loadActivities()


    }

    inner class ActivitiesAdapter : BaseAdapter {
        var listOfActivitiesAdapter = ArrayList<UserActivity>()
        var context: Context? = null

        constructor(context: Context?, listOfActivitiesAdapter: ArrayList<UserActivity>) : super() {
            this.listOfActivitiesAdapter = listOfActivitiesAdapter
            this.context = context
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val activity = listOfActivitiesAdapter[position]
            val layoutInflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            if(activity.actId == "dateDivider"){
                val dateDivider = layoutInflater.inflate(R.layout.date_ticket, null)

                dateDivider.setOnClickListener{
                    //Toast.makeText(context, "click!", Toast.LENGTH_LONG).show()
                }

                val dateYesOrNow = DateHelper.checkTodayOrYesterday(activity.timestamp)
                dateDivider.dateYesOrNowDivider.text = dateYesOrNow
                dateDivider.dateDivider.text = DateHelper.splitTimestampOnlyDate(activity.timestamp)
                return dateDivider
            }

            val actId = activity.actId
            val userView = layoutInflater.inflate(R.layout.mood_ticket, null)
            userView.actName.text = activity.actName
            userView.actScore.text = activity.actScore.toString()
            if(activity.actAltText == ""){
                userView.actAltText.visibility = View.GONE
            } else {
                userView.actAltText.text = activity.actAltText
            }
            val actNameLowerCase = "ic_baseline_" + FileNameStringHelper.newFileName(activity.actName) + "24"
            val actNameToSource = resources.getIdentifier(
                "com.example.happiens:drawable/$actNameLowerCase",
                null,
                null
            )
            userView.actIcon.setImageResource(actNameToSource)
            userView.actDate.text = DateHelper.splitTimestampOnlyTime(activity.timestamp)

            if(activity.actImg == ""){
                userView.btnImg.visibility = View.INVISIBLE
            }

            userView.moodTicket.setOnClickListener{
                val intent = Intent(context, ActivityDetailPage::class.java)
                intent.putExtra("actId", activity.actId)
                intent.putExtra("actIcon", actNameLowerCase)
                intent.putExtra("actName", activity.actName)
                intent.putExtra("actScore", activity.actScore.toString())
                intent.putExtra("actAltText", activity.actAltText)
                intent.putExtra("actDate", activity.timestamp)
                intent.putExtra("detailImage", activity.actImg)
                context!!.startActivity(intent)
            }

            userView.btnEdit.setOnClickListener {
                val intent = Intent(context, EditPage::class.java)
                intent.putExtra("actId", activity.actId)
                intent.putExtra("actIcon", actNameLowerCase)
                intent.putExtra("actName", activity.actName)
                intent.putExtra("actScore", activity.actScore.toString())
                intent.putExtra("actAltText", activity.actAltText)
                intent.putExtra("actDate", activity.timestamp)
                intent.putExtra("detailImage", activity.actImg)
                context!!.startActivity(intent)
            }

            userView.btnDelete.setOnClickListener {
                val builder = AlertDialog.Builder(context!!)
                builder.setTitle("Delete your activity")
                builder.setMessage("Do you want to delete this activity?")
                builder.setPositiveButton("YES"){ _, _ ->
                    dbRef.child("activities").child(actId).removeValue()
                    if(activity.actImg != ""){
                        val gsReference = storage.getReferenceFromUrl(activity.actImg)
                        storageRef.child(gsReference.path).delete().addOnSuccessListener {

                        }.addOnFailureListener {

                        }
                    }
                    Toast.makeText(context!!,"Delete successfully.",Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("No"){ _, _ ->

                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            return userView
        }

        override fun getItem(position: Int): Any {
            return listOfActivitiesAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listOfActivitiesAdapter.size
        }
    }

private fun loadActivities() {
    firebaseAuth = FirebaseAuth.getInstance()
        val authUser = firebaseAuth?.currentUser?.uid
        val UserIdFilterHistory = dbRef.child("activities")
            .orderByChild("userId")
            .equalTo(authUser)


    UserIdFilterHistory.addValueEventListener(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            try {
                listOfActivities.clear()
                    val td = dataSnapshot.value as HashMap<*, *>
                    val sorted = td.toSortedMap(compareByDescending { it.toString() })

                    for (key in sorted.keys) {
                        val act = td[key] as HashMap<*, *>

                        var getdate = act["timestamp"] as String
                        var date = DateHelper.splitTimestampOnlyDate(getdate)
                    if (combineDate != date) {
                        combineDate = date

                        listOfActivities.add(
                            UserActivity(
                                "dateDivider", "", "", 0.0,
                                "", "", act["timestamp"] as String
                            )
                        )

                    }

                    var actScoreString = "" + act["actScore"]
                    var actScore: Double = actScoreString.toDouble()

                    listOfActivities.add(
                        UserActivity(
                            act["actId"] as String,
                            act["userId"] as String,
                            act["actName"] as String,
                            actScore,
                            act["actAltText"] as String,
                            act["actImg"] as String,
                            act["timestamp"] as String
                        )
                    )
                }
                adapter!!.notifyDataSetChanged()
            } catch (ex: Exception) {
            }
        }
    })
}


}