package com.fingeraddress.app

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.fingeraddress.app.databinding.ActivityEnrollBinding
import com.fingeraddress.app.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class EnrollActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnrollBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnrollBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkBiometricSupport()

        binding.btnScanFingerprint.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val fatherMobile = binding.etFatherMobile.text.toString().trim()

            if (name.isEmpty()) {
                binding.etName.error = getString(R.string.error_name_required)
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                binding.etAddress.error = getString(R.string.error_address_required)
                return@setOnClickListener
            }
            if (fatherMobile.isEmpty()) {
                binding.etFatherMobile.error = getString(R.string.error_mobile_required)
                return@setOnClickListener
            }
            if (fatherMobile.length < 10) {
                binding.etFatherMobile.error = getString(R.string.error_mobile_invalid)
                return@setOnClickListener
            }

            showBiometricPrompt(name, address, fatherMobile)
        }
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                binding.tvBiometricStatus.text = getString(R.string.no_fingerprint_hardware)
                binding.btnScanFingerprint.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                binding.tvBiometricStatus.text = getString(R.string.fingerprint_unavailable)
                binding.btnScanFingerprint.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                binding.tvBiometricStatus.text = getString(R.string.no_fingerprint_enrolled)
                binding.btnScanFingerprint.isEnabled = false
            }
            else -> {
                binding.tvBiometricStatus.text = getString(R.string.fingerprint_ready)
            }
        }
    }

    private fun showBiometricPrompt(name: String, address: String, fatherMobile: String) {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    saveProfileToFirebase(name, address, fatherMobile)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(
                            this@EnrollActivity,
                            getString(R.string.fingerprint_error, errString),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(
                        this@EnrollActivity,
                        getString(R.string.fingerprint_not_recognized),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_enroll_title))
            .setSubtitle(getString(R.string.prompt_enroll_subtitle))
            .setDescription(getString(R.string.prompt_enroll_description))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun saveProfileToFirebase(name: String, address: String, fatherMobile: String) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val profile = UserProfile(
            name = name,
            address = address,
            fatherMobile = fatherMobile,
            deviceId = deviceId,
            enrolledAt = System.currentTimeMillis()
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnScanFingerprint.isEnabled = false

        db.collection("profiles")
            .document(deviceId)
            .set(profile)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                showSuccessState(name)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnScanFingerprint.isEnabled = true
                Toast.makeText(
                    this,
                    getString(R.string.save_failed, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showSuccessState(name: String) {
        binding.enrollForm.visibility = View.GONE
        binding.layoutSuccess.visibility = View.VISIBLE
        binding.tvSuccessName.text = getString(R.string.enroll_success_message, name)

        binding.btnDone.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
