package com.fingeraddress.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fingeraddress.app.databinding.ActivityProfileBinding
import com.fingeraddress.app.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private var fatherMobile: String = ""

    companion object {
        const val EXTRA_DEVICE_ID = "extra_device_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID) ?: run {
            showError(getString(R.string.error_no_device_id))
            return
        }

        loadProfile(deviceId)
    }

    private fun loadProfile(deviceId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.profileContent.visibility = View.GONE
        binding.layoutError.visibility = View.GONE

        db.collection("profiles")
            .document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    profile?.let { displayProfile(it) } ?: showError(getString(R.string.error_profile_parse))
                } else {
                    showError(getString(R.string.error_no_profile_found))
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                showError(getString(R.string.error_load_failed, e.message))
            }
    }

    private fun displayProfile(profile: UserProfile) {
        fatherMobile = profile.fatherMobile

        binding.tvName.text = profile.name
        binding.tvAddress.text = profile.address
        binding.tvFatherMobile.text = profile.fatherMobile

        binding.profileContent.visibility = View.VISIBLE

        binding.btnCallFather.setOnClickListener {
            callNumber(fatherMobile)
        }

        binding.btnShareLocation.setOnClickListener {
            shareProfile(profile)
        }
    }

    private fun callNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
    }

    private fun shareProfile(profile: UserProfile) {
        val shareText = getString(
            R.string.share_profile_text,
            profile.name,
            profile.address,
            profile.fatherMobile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.profileContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message

        binding.btnRetry.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
