package com.simats.scanmypills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityHelpSupportBinding

@Suppress("DEPRECATION")
class HelpSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        populateFaqs()
        populateGuides()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.btnEmailSupport.setOnClickListener {
            Toast.makeText(this, "Redirecting to Email...", Toast.LENGTH_SHORT).show()
        }
        binding.btnChatSupport.setOnClickListener {
            Toast.makeText(this, "Connecting to Support...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateFaqs() {
        val faqs = listOf(
            "How do I scan a medicine package correctly?" to "To scan a package, click the 'Scan' button on the home screen. Place the package in front of the camera and ensure there is good lighting. Wait for the AI to recognize the box and provide details.",
            "Can the app identify loose tablets?" to "Yes, use the 'Identify Tablet' feature. Place a single tablet on a plain, contrasting surface and capture a clear top-down photo for the most accurate identification results.",
            "How are expiry dates detected?" to "The app uses OCR (Optical Character Recognition) to find expiry dates printed on medicine packaging during the scanning process. Always verify the detected date with the physical box.",
            "How do medicine reminders work?" to "Once you scan or add a medicine, you can set daily dosage times. The app will send you notifications at those exact times to ensure you never miss a dose.",
            "Can I edit or delete saved medicines?" to "Yes, in 'All Medicines' section, tap on any medicine to view details. You can then use the edit icon to change information or the delete icon to remove it from your list.",
            "How is my medicine data protected?" to "Your health data is encrypted and stored securely. We do not share your personal medication history with third parties without your explicit permission.",
            "What should I do if I forgot my password?" to "On the login screen, click 'Forgot Password'. We will send an OTP to your registered email address to help you securely reset your password.",
            "Can I manage medicines for family members?" to "Currently, you can add multiple medicines to one profile. We are working on a 'Profiles' feature to separate medication lists for different family members."
        )

        faqs.forEach { (question, answer) ->
            addExpandableItem(binding.faqContainer, question, answer, null)
        }
    }

    private fun populateGuides() {
        val guides = listOf(
            Triple("Getting Started", "Learn the basics of setting up your profile and adding your first medication.", R.drawable.ic_smartphone),
            Triple("Scanning Medicine Packages", "A step-by-step guide on how to get the best results when scanning medicine boxes.", R.drawable.ic_box),
            Triple("Identifying Loose Tablets", "Tips and tricks for identifying individual pills without their original packaging.", R.drawable.ic_pill),
            Triple("Setting Medicine Reminders", "How to configure complex schedules and notification preferences for your meds.", R.drawable.ic_clock),
            Triple("Managing Medicine Storage", "Understand how the app tracks your current stock and notifies you when running low.", R.drawable.ic_video)
        )

        guides.forEach { (title, content, iconRes) ->
            addExpandableItem(binding.guidesContainer, title, content, iconRes)
        }
    }

    private fun addExpandableItem(container: LinearLayout, title: String, content: String, iconRes: Int?) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_expandable_help, container, false)
        val titleLayout = itemView.findViewById<LinearLayout>(R.id.titleLayout)
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvContent = itemView.findViewById<TextView>(R.id.tvContent)
        val ivArrow = itemView.findViewById<ImageView>(R.id.ivArrow)
        val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)

        tvTitle.text = title
        tvContent.text = content
        
        if (iconRes != null) {
            ivIcon.visibility = View.VISIBLE
            ivIcon.setImageResource(iconRes)
            // Color tint based on icon type (matching image)
            when(title) {
                "Getting Started" -> ivIcon.setBackgroundResource(R.drawable.bg_icon_rounded_blue)
                "Scanning Medicine Packages" -> {
                    ivIcon.setBackgroundResource(R.drawable.bg_icon_rounded_green)
                    ivIcon.setColorFilter(getColor(R.color.icon_reminders))
                }
                "Identifying Loose Tablets" -> {
                    ivIcon.setBackgroundResource(R.drawable.bg_icon_rounded_blue)
                    ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5E6FF.toInt()))
                    ivIcon.setColorFilter(0xFF8B5CF6.toInt())
                }
                "Setting Medicine Reminders" -> {
                    ivIcon.setBackgroundResource(R.drawable.bg_icon_rounded_blue)
                    ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFF7ED.toInt()))
                    ivIcon.setColorFilter(0xFFF97316.toInt())
                }
                "Managing Medicine Storage" -> {
                    ivIcon.setBackgroundResource(R.drawable.bg_icon_rounded_blue)
                    ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEF2F2.toInt()))
                    ivIcon.setColorFilter(0xFFEF4444.toInt())
                }
            }
        } else {
            ivIcon.visibility = View.GONE
        }

        titleLayout.setOnClickListener {
            if (tvContent.visibility == View.GONE) {
                tvContent.visibility = View.VISIBLE
                ivArrow.animate().rotation(180f).setDuration(200).start()
            } else {
                tvContent.visibility = View.GONE
                ivArrow.animate().rotation(0f).setDuration(200).start()
            }
        }

        container.addView(itemView)
        
        // Add divider if not last
        val divider = View(this)
        divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        divider.setBackgroundColor(getColor(R.color.bg_light_grey))
        container.addView(divider)
    }
}
