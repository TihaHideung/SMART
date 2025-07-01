package com.example.smart

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ActivityCreateProfil : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var textName: TextView
    private var selectedImageUri: Uri? = null
    private var userId: Int = -1

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 200
        private const val REQUEST_PERMISSION_GALLERY = 201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profil)

        imageProfile = findViewById(R.id.imageProfile)
        textName = findViewById(R.id.text_name)
        val launchBtn = findViewById<Button>(R.id.btn_launch)
        val backBtn = findViewById<ImageView>(R.id.btnBack)
        val iconCamera = findViewById<ImageView>(R.id.iconCamera)

        // Ambil nama dari signup dan tampilkan
        val nameFromSignup = intent.getStringExtra("name")
        if (!nameFromSignup.isNullOrEmpty()) {
            textName.text = nameFromSignup
        }

        // Ambil user_id dari SharedPreferences
        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "ID user tidak ditemukan, harap login ulang", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        backBtn.setOnClickListener {
            startActivity(Intent(this, ActivitySignup::class.java))
            finish()
        }

        imageProfile.setOnClickListener { checkGalleryPermissionAndOpen() }
        iconCamera.setOnClickListener { checkGalleryPermissionAndOpen() }

        launchBtn.setOnClickListener {
            val name = textName.text.toString().trim()

            if (selectedImageUri == null) {
                Toast.makeText(this, "Pilih foto profil terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val realPath = getRealPathFromURI(selectedImageUri!!)
            if (realPath.isEmpty()) {
                Toast.makeText(this, "Gagal mengambil path gambar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val file = File(realPath)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val imageBody = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val userIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
            val nameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)

            RetrofitClient.instance.uploadProfile(imageBody, userIdBody, nameBody)
                .enqueue(object : Callback<DefaultResponse> {
                    override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(this@ActivityCreateProfil, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ActivityCreateProfil, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@ActivityCreateProfil, "Gagal menyimpan profil: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                        Toast.makeText(this@ActivityCreateProfil, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun checkGalleryPermissionAndOpen() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_GALLERY)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_GALLERY && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Izin dibutuhkan untuk mengakses galeri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imageProfile.setImageURI(selectedImageUri)
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path = cursor.getString(columnIndex).also { cursor.close() }
            path
        } else {
            ""
        }
    }
}
