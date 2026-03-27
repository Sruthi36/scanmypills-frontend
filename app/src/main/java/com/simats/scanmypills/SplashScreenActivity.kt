package com.simats.scanmypills

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        applyAppNameGradient()
        startEntranceAnimation()
        startCinematicBackgroundMotion()

        // Start Title Shimmer after 1s delay
        Handler(Looper.getMainLooper()).postDelayed({
            startTitleShimmer()
        }, 1000)

        // Wait for 2.5 seconds and then navigate to OnboardingActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }, 2500)
    }

    private fun startTitleShimmer() {
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val paint = tvAppName.paint
        val width = paint.measureText(tvAppName.text.toString())

        val shimmerShader = LinearGradient(
            -width, 0f, 0f, 0f,
            intArrayOf(
                ContextCompat.getColor(this, R.color.ice_blue),
                ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, R.color.ice_blue_dark)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        val matrix = Matrix()
        ValueAnimator.ofFloat(0f, width * 2f).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val translate = animator.animatedValue as Float
                matrix.setTranslate(translate, 0f)
                shimmerShader.setLocalMatrix(matrix)
                tvAppName.paint.shader = shimmerShader
                tvAppName.invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    applyAppNameGradient()
                }
            })
            start()
        }
    }

    private fun applyAppNameGradient() {
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val paint = tvAppName.paint
        val width = paint.measureText(tvAppName.text.toString())
        
        val textShader: Shader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                ContextCompat.getColor(this, R.color.ice_blue),
                ContextCompat.getColor(this, R.color.ice_blue_dark)
            ),
            null, Shader.TileMode.CLAMP
        )
        tvAppName.paint.shader = textShader
    }

    private fun startEntranceAnimation() {
        val background = findViewById<ImageView>(R.id.iv_background)
        val overlay = findViewById<View>(R.id.overlay_gradient)
        val title = findViewById<TextView>(R.id.tv_app_name)
        val subtitle = findViewById<TextView>(R.id.tv_subtitle)
        val tagline = findViewById<TextView>(R.id.tv_tagline)
        val dotsLayout = findViewById<View>(R.id.layout_indicators)
        val poweredBy = findViewById<TextView>(R.id.tv_powered_by)

        // 1. Background and Overlay Fade-in (300ms)
        background.animate().alpha(1f).setDuration(300).start()
        overlay.animate().alpha(1f).setDuration(300).start()

        // 2. Title Fade + Scale (400ms)
        title.apply {
            scaleX = 0.95f
            scaleY = 0.95f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .start()
        }

        // 3. Subtitle and Tagline Fade-in with 150ms delay
        subtitle.animate().alpha(1f).setDuration(400).setStartDelay(150).start()
        tagline.animate().alpha(1f).setDuration(400).setStartDelay(150).start()

        // 4. Powered by Fade-in with 300ms delay
        poweredBy.animate().alpha(1f).setDuration(500).setStartDelay(300).start()

        // 5. Dots appear and start wave animation after 500ms
        dotsLayout.animate().alpha(1f).setDuration(300).setStartDelay(500).withEndAction {
            startPulseAnimation()
        }.start()
    }

    private fun startCinematicBackgroundMotion() {
        val background = findViewById<ImageView>(R.id.iv_background)

        // 1. Zoom: 1.0 -> 1.08 (8s, No repeat)
        ObjectAnimator.ofFloat(background, View.SCALE_X, 1f, 1.08f).apply {
            duration = 8000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(background, View.SCALE_Y, 1f, 1.08f).apply {
            duration = 8000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        // 2. Rotational Float: -3° to +3° (6s, Reverse, Infinite)
        ObjectAnimator.ofFloat(background, View.ROTATION, -3f, 3f).apply {
            duration = 6000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        // 3. Parallax Vertical Float: -10dp to +10dp (7s, Reverse, Infinite)
        val floatPx = 10 * resources.displayMetrics.density
        ObjectAnimator.ofFloat(background, View.TRANSLATION_Y, -floatPx, floatPx).apply {
            duration = 7000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun startPulseAnimation() {
        val dot1 = findViewById<ImageView>(R.id.dot1)
        val dot2 = findViewById<ImageView>(R.id.dot2)
        val dot3 = findViewById<ImageView>(R.id.dot3)

        val dots = listOf(dot1, dot2, dot3)
        
        dots.forEachIndexed { index, dot ->
            val scaleX = ObjectAnimator.ofFloat(dot, View.SCALE_X, 1.0f, 1.6f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(dot, View.SCALE_Y, 1.0f, 1.6f, 1.0f)
            
            val pulse = AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 500
                startDelay = (index * 120).toLong()
                interpolator = AccelerateDecelerateInterpolator()
            }
            
            pulse.addListener(object : android.animation.AnimatorListenerAdapter() {
                var isFirstRun = true
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    pulse.startDelay = (3 * 120 - 500).toLong().coerceAtLeast(0)
                    pulse.start()
                }
            })
            pulse.start()
        }
    }
}
