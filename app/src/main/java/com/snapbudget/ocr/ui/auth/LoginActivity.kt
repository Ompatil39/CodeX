package com.snapbudget.ocr.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.snapbudget.ocr.MainActivity
import com.snapbudget.ocr.databinding.ActivityLoginBinding

/**
 * Standalone login / registration screen.
 *
 * The user can toggle between "Login" and "Register" modes.
 * On success the activity navigates to [MainActivity] and finishes itself
 * so the user cannot press Back to return here.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    /** true = currently showing the Register form; false = Login form. */
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        observeAuthState()
    }

    // ─── UI wiring ───────────────────────────────────────────────────────

    private fun setupUi() {
        updateFormMode()

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!validateInput(email, password)) return@setOnClickListener

            if (isRegisterMode) {
                val name = binding.etDisplayName.text.toString().trim()
                if (name.isEmpty()) {
                    binding.tilDisplayName.error = "Enter your name"
                    return@setOnClickListener
                }
                viewModel.register(email, password, name)
            } else {
                viewModel.login(email, password)
            }
        }

        binding.btnToggleMode.setOnClickListener {
            isRegisterMode = !isRegisterMode
            viewModel.resetState()
            clearErrors()
            updateFormMode()
        }
    }

    private fun updateFormMode() {
        if (isRegisterMode) {
            binding.txtTitle.text = "Create Account"
            binding.tilDisplayName.visibility = View.VISIBLE
            binding.btnSubmit.text = "Register"
            binding.btnToggleMode.text = "Already have an account? Login"
        } else {
            binding.txtTitle.text = "Welcome Back"
            binding.tilDisplayName.visibility = View.GONE
            binding.btnSubmit.text = "Login"
            binding.btnToggleMode.text = "Don't have an account? Register"
        }
    }

    // ─── Validation ──────────────────────────────────────────────────────

    private fun validateInput(email: String, password: String): Boolean {
        clearErrors()
        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            valid = false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            valid = false
        }
        return valid
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilDisplayName.error = null
        binding.txtError.visibility = View.GONE
    }

    // ─── Observe ViewModel ───────────────────────────────────────────────

    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                }
                is AuthViewModel.AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                    binding.txtError.visibility = View.GONE
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    binding.txtError.visibility = View.VISIBLE
                    binding.txtError.text = state.message
                }
            }
        }
    }

    // ─── Navigation ──────────────────────────────────────────────────────

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
