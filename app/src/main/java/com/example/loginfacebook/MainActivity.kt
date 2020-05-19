package com.example.loginfacebook

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.bumptech.glide.Glide
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    val callBack = CallbackManager.Factory.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



login.setOnClickListener {
    login_button.callOnClick()
}
        login_button.setReadPermissions(arrayListOf("public_profile","email"))
        login_button.registerCallback(callBack, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val request = GraphRequest.newMeRequest(result?.accessToken) { `object`, _ ->
                    tvName.text = `object`.getString("email")
                    val image = "https://graph.facebook.com/" + result?.accessToken!!.userId+"/picture?return_sll_resource=1"

                    Glide.with(this@MainActivity).load(image).into(ivProfile)
                    Log.d("public_profile",`object`.toString())
                }
                tvName.text = result?.accessToken!!.userId

                val bundle = Bundle()
                bundle.putString("fields","email")
                request.parameters = bundle
                request.executeAsync()

                LoginManager.getInstance().logOut()
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
                Log.d("error",error?.localizedMessage)
            }

        })
//        getKeyHash()
    }

    private fun getKeyHash() {
        val infor = packageManager.getPackageInfo("com.example.loginfacebook",PackageManager.GET_SIGNATURES)
        for(signature in infor.signatures){
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.d("KeyHash",Base64.encodeToString(md.digest(),Base64.DEFAULT))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callBack.onActivityResult(requestCode,resultCode,data)
    }
}
