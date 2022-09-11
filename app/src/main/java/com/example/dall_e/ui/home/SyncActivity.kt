package com.example.dall_e.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.dall_e.databinding.AcitivitySyncBinding

class SyncActivity : AppCompatActivity() {

    private lateinit var binding: AcitivitySyncBinding
    var appCompatActivity: AppCompatActivity = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AcitivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val flagLogout = intent.getBooleanExtra("logout", false)

        val toolbar: Toolbar = binding.toolbar
        val account = Auth0(this)


        if (flagLogout) {
            WebAuthProvider.logout(account)
                .start(this, object : Callback<Void?, AuthenticationException> {
                    override fun onFailure(error: AuthenticationException) {
//                        Toast.makeText(applicationContext, "Failure", Toast.LENGTH_LONG).show()
                    }

                    override fun onSuccess(result: Void?) {
//                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
                    }

                })
        } else {

            WebAuthProvider.login(account)
                .start(this, callback)
        }


    }

    val callback = object : Callback<Credentials, AuthenticationException> {
        override fun onFailure(exception: AuthenticationException) {
            // Failure! Check the exception for details
            Toast.makeText(applicationContext, "Login Failure", Toast.LENGTH_LONG).show()
            finish()

        }

        override fun onSuccess(credentials: Credentials) {
            // Success! Access token and ID token are presents
            Toast.makeText(
                applicationContext,
                "Login Success, Will Sync to this account",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }


}