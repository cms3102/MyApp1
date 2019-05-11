package com.csergio.myapp1.user

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.csergio.myapp1.MainActivity
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody


class SignUpActivity : AppCompatActivity() {

    private val PICK_FROM_ALBUM = 10
    private val PICK_FROM_CAMERA = 11
    private lateinit var imageUri: Uri
    private var imageAbsolutePath = ""
    private lateinit var imageFile:File
    private var email = ""
    private var profileImageUri = ""
    private var isSelectedProfileImage = false
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.csergio.myapp1.R.layout.activity_sign_up)

        preferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)

        signUpActivity_profileImage.setOnClickListener {

            val dialog = AlertDialog.Builder(this)
                .setTitle("이미지 소스 선택")
                .setPositiveButton("카메라") { dialog, which ->

                    val camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    val externalWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                    if (camera && externalWrite){

                        var state = Environment.getExternalStorageState()
                        if (state == Environment.MEDIA_MOUNTED){

                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            if(intent.resolveActivity(packageManager) != null){
                                try {

                                    // 안드로이드 7.0부터 보안 문제로 Uri.fromFile() 오류 발생해서 FileProvider 써야 됨
                                    val file = createImageFile()
                                    val filUri = FileProvider.getUriForFile(this, packageName, file)

                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, filUri)
                                    startActivityForResult(intent, PICK_FROM_CAMERA)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }

                        }

                    }

                }
                .setNegativeButton("갤러리") { dialog, which ->
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = MediaStore.Images.Media.CONTENT_TYPE
                    startActivityForResult(intent, PICK_FROM_ALBUM)
                }
                .setNeutralButton("취소") { dialog, which ->
                    dialog.cancel()
                }
                .create()

            dialog.show()

        }

        signUpActivity_signUpButton.setOnClickListener {

            email = signUpActivity_editText_email.text.toString()
            val name = signUpActivity_editText_name.text.toString()
            val pw1 = signUpActivity_editText_pw1.text.toString()
            val pw2 = signUpActivity_editText_pw2.text.toString()

            if (email.isNullOrEmpty() || name.isNullOrEmpty() || pw1.isNullOrEmpty() || pw2.isNullOrEmpty()){
                Toast.makeText(this, "가입 양식을 모두 채워주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw1 != pw2) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isSelectedProfileImage){
                Toast.makeText(this, "프로필 이미지를 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userIdRequestBody = RequestBody.create(MultipartBody.FORM, email)
            val fileRequestBody = RequestBody.create(MediaType.parse(contentResolver.getType(imageUri)), imageFile)
            val part = MultipartBody.Part.createFormData("image", imageFile.name, fileRequestBody)
            Log.d("이미지 URI", "$imageUri")
            Log.d("이미지 경로", "${imageUri.path}")
            Log.d("이미지 파일 이름", "${imageFile.name}")

            // 프로필 사진 서버 업로드
            RetrofitBuilder.retrofit.create(PostService::class.java)
                .uploadProfileImage(userIdRequestBody, part).enqueue(object:Callback<String>{

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@SignUpActivity, "프로필 이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        profileImageUri = response.body().toString()
                        Toast.makeText(this@SignUpActivity, "프로필 이미지 업로드 성공 : $profileImageUri", Toast.LENGTH_SHORT).show()

                        val newUser = User()
                        newUser.user_id = email
                        newUser.user_pw = pw1
                        newUser.user_name = name
                        newUser.user_pic = profileImageUri

                        // 회원 가입 처리
                        RetrofitBuilder.retrofit.create(PostService::class.java)
                            .requestSignUpPost(newUser).enqueue(object : Callback<String> {
                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Toast.makeText(this@SignUpActivity, "회원 가입 실패", Toast.LENGTH_SHORT).show()
                            }

                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful) {

                                    Toast.makeText(this@SignUpActivity, "회원 가입 성공", Toast.LENGTH_SHORT).show()

                                    // 로그인 처리
                                    val user = User()
                                    user.user_id = email
                                    user.user_pw = pw1

                                    RetrofitBuilder.retrofit.create(PostService::class.java)
                                        .requestLogin(user).enqueue(object : Callback<String>{
                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                Toast.makeText(this@SignUpActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                                            }

                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful && response.body()?.substringBefore("/") == "true"){

                                                    val body = response.body()
                                                    // 자동 로그인 처리를 위한 아이디 저장
                                                    preferences.edit()
                                                        .putString("user_id", user.user_id)
                                                        .putString("user_name", body?.substringAfter("/"))
                                                        .apply()

                                                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                                                    Toast.makeText(this@SignUpActivity, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                                                    finish()

                                                }
                                            }

                                        })
                                }
                            }

                        })

                    }

                })



        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) {

            isSelectedProfileImage = true

            imageUri = data?.data!!
            val realPath = getRealPathFromURIPath(imageUri, this)
            imageFile = File(realPath)
            Log.d("이미지 URI", "$imageUri")
            Log.d("이미지 경로", "${imageUri.path}")
            signUpActivity_profileImage.setImageURI(imageUri)
            Toast.makeText(this, "사진이 선택되었습니다.", Toast.LENGTH_SHORT).show()

        } else if(requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK){

            isSelectedProfileImage = true

            signUpActivity_profileImage.setImageURI(imageUri)
            // 사진 저장 후 sendBroadcast로 안돼서 미디어 스캐너로 갤러리 갱신
            val mediaScanner = MediaScanner()
            mediaScanner.connection.connect()
            Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()

        }

    }

    inner class MediaScanner:MediaScannerConnection.MediaScannerConnectionClient{

        val connection = MediaScannerConnection(applicationContext, this)

        override fun onMediaScannerConnected() {
            connection.scanFile(imageAbsolutePath, null)
            Log.d("미디어 스캐너", "미디어 스캔 시작")
        }

        override fun onScanCompleted(path: String?, uri: Uri?) {
            connection.disconnect()
            Log.d("미디어 스캐너", "미디어 스캔 종료")
        }

    }

    private fun createImageFile():File{
        val fileName = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MySpace")
        if (!storagePath.isDirectory){
            storagePath.mkdir()
        }
        Log.d("사진 저장 경로", "사진 저장 경로 : $storagePath")
        imageFile = File.createTempFile(fileName, ".jpg", storagePath)
        imageUri = FileProvider.getUriForFile(this, packageName, imageFile)
        imageAbsolutePath = imageFile.absolutePath

        return imageFile
    }

    private fun getRealPathFromURIPath(contentURI: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
        return if (cursor == null) {
            contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(idx)
        }
    }

}
