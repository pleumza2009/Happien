package com.example.happiens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.happiens.dataclass.DateHelper
import com.example.happiens.dataclass.UserActivity
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_record_page.*
import java.io.ByteArrayOutputStream


class RecordPage : AppCompatActivity() {

    lateinit var actRadioGroup: RadioGroup
    var actId:String? = null
    var moodScore = 0.0
    var uploadDate:String? = null
    var imgUrl:String? = null

    var firebaseAuth: FirebaseAuth? = null
    var userUploadImage: Bitmap? = null

    var database = FirebaseDatabase.getInstance()
    var dbRef = database.reference
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
        setContentView(R.layout.activity_record_page)

        firebaseAuth = FirebaseAuth.getInstance()

        todayDate.text = DateHelper.getDate()
        todayTime.text = DateHelper.getTime()

        /*actRadioGroup = findViewById(R.id.actRadioGroup)*/

        moodSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                moodScore = i.toDouble() / 10
                if(moodScore == 10.0 || moodScore == 0.0){
                    var intmoodscore = moodScore.toInt()
                    textViewSeekBar.text = "$intmoodscore"
                } else {
                    textViewSeekBar.text = "$moodScore"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        btnAddMediaFromDevice.setOnClickListener{
            checkpermission()
        }

        btnClearSelectedPicture.setOnClickListener{
            displaySelectedImage.setImageResource(0)
            userUploadImage = null
        }

        btnSaveRecord.setOnClickListener {
                val ActivityCh = chipGroup.findViewById<Chip>(chipGroup.checkedChipId).text
                if (ActivityCh == null) {
                    Toast.makeText(this, "Please select your activity!", Toast.LENGTH_LONG).show()
                } else {
//                    val radioButton = findViewById<RadioButton>(actRadioGroup.checkedRadioButtonId)
                    val activity = ActivityCh.toString()
                    val userAct: List<UserActivity>
                    uploadDate = DateHelper.getTimestamp()
                    if(userUploadImage != null){
                        saveImageIntoFirebase()
                    }
                    userAct = mutableListOf(
                        UserActivity("", firebaseAuth!!.currentUser!!.uid, activity, moodScore, altEditText.text.toString(), "", uploadDate.toString())
                    )
                    userAct.forEach {
                        val key = dbRef.child("activities").push().key!!
                        it.actId = key
                        actId = key
                        dbRef.child("activities").child(key).setValue(it)
                    }
                    finish()
                }
            }
        }


    val READIMAGE:Int=253
    fun checkpermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READIMAGE)
            }
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            READIMAGE->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadImage()
                } else {
                    Toast.makeText(this, "Cannot access your images", Toast.LENGTH_LONG).show()
                }
            }
            else->super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    val PICK_IMAGE_CODE=123
    fun loadImage(){
        var intent=Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_CODE && data!=null){
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor.moveToFirst()
            val columnIndex =  cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            val ei = ExifInterface(picturePath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            var rotatedBitmap: Bitmap? = null
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(BitmapFactory.decodeFile(picturePath), 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(BitmapFactory.decodeFile(picturePath), 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(BitmapFactory.decodeFile(picturePath), 270)
                ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = BitmapFactory.decodeFile(picturePath)
                else -> rotatedBitmap = BitmapFactory.decodeFile(picturePath)
            }
            displaySelectedImage.setImageBitmap(rotatedBitmap)
            userUploadImage = rotatedBitmap
        }
    }

    fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun saveImageIntoFirebase(){
        Toast.makeText(this,"Uploading...",Toast.LENGTH_SHORT).show()
        var currentUser = firebaseAuth!!.currentUser
        val email = currentUser!!.email.toString()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://happiens-d1b51.appspot.com")
        val imagePath = splitString(email) + "_" + uploadDate.toString() + ".jpg"
        val userImagePath = splitString(email)
        val imageRef = storageRef.child("images/$userImagePath/$imagePath")
        imgUrl = "gs://happiens-d1b51.appspot.com/images/$userImagePath/$imagePath"

        val baos = ByteArrayOutputStream()
        userUploadImage!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext, "Fail to Upload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->
            var url = taskSnapshot.storage.downloadUrl.toString()
            dbRef.child("activities").child(actId!!).child("actImg").setValue(imgUrl)
            Toast.makeText(this,"Image Uploaded",Toast.LENGTH_SHORT).show()
        }
    }

    fun splitString(email:String):String{
        val split = email.split("@")
        return split[0]
    }
}
