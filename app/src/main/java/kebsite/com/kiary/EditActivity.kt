package kebsite.com.kiary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class EditActivity : AppCompatActivity(){

    companion object {
        private const val PERMISSION_ALL = 1
        private const val REQUEST_TAKE_PHOTO = 1
        private const val RESULT_LOAD_IMG = 87
        private val permissions : Array<String> = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA)
    }

    private var date: String? = null
    private var number: Int? = null
    private var deletePosition: String? = null
    private var mCurrentPhotoPath: String? = null
    private var mImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_edit)
        getXML()
    }

    private fun getXML() {
        val b = intent.extras
        val dateTxt = findViewById<TextView>(R.id.date_txt)
        val diaryEdit = findViewById<EditText>(R.id.diary_edit)
        mImageView = findViewById(R.id.mImageView)
        val modify = b?.getBoolean("modify", false)
        if(modify!!) {
            mCurrentPhotoPath = b.getString("picture")
            setPic()
            diaryEdit.setText(b.getString("content"))
            deletePosition = b.getString("delete")
        }
        val day = b.getString("date")
        date = day
        number = b.getInt("number")
        dateTxt.text = day
        dateTxt.textSize = 20f
        val button = findViewById<Button>(R.id.close_btn)
        button.setOnClickListener {
            onBackPressed()
        }
        diaryEdit.inputType = InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
        diaryEdit.setSingleLine(false)
        diaryEdit.setOnFocusChangeListener { _, f->
            if(f) {
                diaryEdit.hint = ""
            }
            else {
                diaryEdit.hint = resources.getString(R.string.hint)
            }
        }
        val sendBtn = findViewById<Button>(R.id.send_btn)
        sendBtn.setOnClickListener {view->
            val content = diaryEdit.text.toString()
            if(content!="")
                storeDiary(content)
            else {
                Snackbar.make(view, resources.getString(R.string.edit_blank), Snackbar.LENGTH_SHORT).show()
            }
        }
        val cameraBtn = findViewById<Button>(R.id.camera_btn)
        cameraBtn.setOnClickListener{
            getPermissions()
        }
        val galleryBtn = findViewById<Button>(R.id.gallery_btn)
        galleryBtn.setOnClickListener {
            getImageFromGallery()
        }
    }

    private fun getPermissions() {

        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL)
        }
        else {
            dispatchTakePictureIntent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_ALL -> {
                for(permission in permissions) {
                    Log.d("request", permission)
                }
                for(result:Int in grantResults) {
                    Log.d("get", result.toString())
                    if(result != PackageManager.PERMISSION_GRANTED)
                        return
                }
                dispatchTakePictureIntent()
            }
        }
    }
    private fun hasPermissions(context: Context, permissions: Array<String>) : Boolean{
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun getImageFromGallery() {
        Intent(Intent.ACTION_PICK).also {getPhotoIntent ->
            getPhotoIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {file->
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileProvider",
                        file
                    )
                    getPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    getPhotoIntent.type = "image/*"
                    startActivityForResult(getPhotoIntent, RESULT_LOAD_IMG)
                }
            }

        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {file->
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileProvider",
                        file
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.TAIWAN).format(Date())
        Log.d("time", timeStamp)
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
            galleryAddPic()
        }
    }
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(mCurrentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        //val targetW: Int = mImageView?.width!!
        //val targetH: Int = mImageView?.height!!

        val metrics = resources.displayMetrics
        val targetW = (100 * metrics.density).toInt()
        val targetH = (100 * metrics.density).toInt()
        Log.d("width", targetW.toString())

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(mCurrentPhotoPath, this)
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            //inPurgeable = true
        }
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)?.also { bitmap ->
            mImageView?.setImageBitmap(bitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic()
        }
        else if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK){
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN).format(Date())
            val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val myFile = File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                mCurrentPhotoPath = absolutePath
            }
            val imageUri = data?.data
            val imageStream = contentResolver.openInputStream(imageUri!!)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            val out = FileOutputStream(myFile)
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, out)
            mImageView?.setImageBitmap(selectedImage)
        }
    }

    override fun finish() {
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
        super.finish()
    }

    override fun onBackPressed() {
        val i = Intent(this, MainActivity::class.java)
        setResult(404 ,i)
        finish()
        super.onBackPressed()
    }

    private fun storeDiary(content:String) {
        Log.d("content", content)
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        Log.d("time", time.toString())
        val sp = getSharedPreferences("diaryData", Activity.MODE_PRIVATE)
        if(deletePosition!=null) {
            sp?.edit()?.remove(deletePosition)?.apply()
        }
        val id = sp.getInt(date+"id", 0)
        val editor = sp.edit()
        Log.d("date", date)
        editor.putString(date+id, content)
        editor.putInt(date+"id", id+1)
        editor.putString(date+"picture"+id, mCurrentPhotoPath)
        editor.putString(date+"editTime"+id, time)
        editor.putString(date+"type"+id, "diary")
        if(editor.commit()) {
            val i = Intent(this, MainActivity::class.java)
            val b = Bundle()
            b.putInt("number", number!!)
            i.putExtras(b)
            setResult(200 ,i)
            finish()
        }
    }
}
