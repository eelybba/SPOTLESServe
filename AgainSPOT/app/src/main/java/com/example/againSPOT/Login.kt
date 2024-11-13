package com.example.againSPOT

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.againSPOT.databinding.ActivityLoginBinding
import com.example.againSPOT.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Login : AppCompatActivity() {

    private lateinit var emailLogin: String
    private lateinit var passwordLogin: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding:ActivityLoginBinding by lazy{
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance().reference

        // Initialize Google Sign-In options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)


        //login with email and password
        binding.loginBtn.setOnClickListener {
            //get data from text field
            emailLogin = binding.emailLogin.text.toString().trim()
            passwordLogin = binding.passwordLogin.text.toString().trim()

            if (emailLogin.isBlank() || passwordLogin.isBlank()) {
                Toast.makeText(this, "Please fill in all the required details!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(emailLogin, passwordLogin)
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
            }
        }

        binding.GoogleBtn.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        binding.loginBtn.setOnClickListener{
            emailLogin = binding.emailLogin.text.toString().trim()
            passwordLogin = binding.passwordLogin.text.toString().trim()

            if (emailLogin.isBlank() || passwordLogin.isBlank()) {
                Toast.makeText(this, "Please fill in all the required details!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(emailLogin, passwordLogin)
            }
        }

        binding.GoogleBtn.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        binding.donthavebutton.setOnClickListener{
            val intent= Intent(this,Register::class.java)
            startActivity(intent)
        }
        binding.forgotPasswordBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(emailLogin: String, passwordLogin: String) {
        auth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                updateUi(user)
            } else {
                Toast.makeText(this, "Login Failed, Please Enter Correct Information", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account: GoogleSignInAccount? = task.result
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login  Successfully !!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to authenticate with Google", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Google Login  failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Google Login  cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(Exception::class.java)
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Toast.makeText(this, "Sign in with Google successfully", Toast.LENGTH_SHORT).show()
                        updateUi(authTask.result?.user)
                        finish()
                    } else {
                        Toast.makeText(this, "Sign in with Google failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Sign in with Google failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun updateUi(user: FirebaseUser?) {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}


