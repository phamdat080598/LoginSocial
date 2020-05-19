package com.example.loginfacebook

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 1
    private var mGoogleSignInClient: GoogleSignInClient? = null
    val callBack = CallbackManager.Factory.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getKeyHash()

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        login.setOnClickListener {
            login_button.callOnClick()
        }
        login_button.setReadPermissions(arrayListOf("public_profile", "email"))
        login_button.registerCallback(callBack, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val request = GraphRequest.newMeRequest(result?.accessToken) { `object`, _ ->
                    tvName.text = `object`.getString("email")
                    val image =
                        "https://graph.facebook.com/" + result?.accessToken!!.userId + "/picture?return_sll_resource=1"

                    Glide.with(this@MainActivity).load(image).into(ivProfile)
                    Log.d("public_profile", `object`.toString())
                }
                tvName.text = result?.accessToken!!.userId

                val bundle = Bundle()
                bundle.putString("fields", "email")
                request.parameters = bundle
                request.executeAsync()

                LoginManager.getInstance().logOut()
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
                Log.d("error", error?.localizedMessage)
            }

        })

        btnGoogle.setOnClickListener {
            val signInIntent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        btnLogoutGG.setOnClickListener {
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()
//        val account = GoogleSignIn.getLastSignedInAccount(this)
//        tvName.text = account!!.displayName
//        Glide.with(this@MainActivity).load(account.photoUrl).into(ivProfile)
        // Signed in successfully, show authenticated UI.
    }

    private fun getKeyHash() {
        val infor = packageManager.getPackageInfo(
            "com.example.loginfacebook",
            PackageManager.GET_SIGNATURES
        )
        for (signature in infor.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callBack.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) { // The Task returned from this call is always completed, no need to attach
// a listener.
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            tvName.text = account!!.displayName
            Glide.with(this@MainActivity).load(account.photoUrl).into(ivProfile)
            // Signed in successfully, show authenticated UI.

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("Error", "signInResult:failed code=" + e.statusCode);
        }
    }

    private fun signOut() {
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener(this, object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    Toast.makeText(this@MainActivity,"logout complete",Toast.LENGTH_SHORT).show()
                }
            })
    }

}
