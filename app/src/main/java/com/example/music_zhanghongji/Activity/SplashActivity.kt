package com.example.music_zhanghongji.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.music_zhanghongji.R

class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dimOverlay: View
    private lateinit var privacyPopup: View
    private lateinit var btnAgree: TextView
    private lateinit var btnDisagree: TextView
    private lateinit var tvPrivacy: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("music_prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)

        if (!isFirstLaunch) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_splash)

        dimOverlay = findViewById(R.id.dim_overlay)
        privacyPopup = findViewById(R.id.privacy_popup)
        btnAgree = findViewById(R.id.btn_agree)
        btnDisagree = findViewById(R.id.btn_disagree)
        tvPrivacy = findViewById(R.id.tv_privacy_text)

        // 自定义 SpannableString 让“用户协议”和“隐私政策”变蓝且无下划线且可点击
        val text = "欢迎使用音乐社区！我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》。"
        val spannableString = SpannableString(text)

        val userProtocolStart = text.indexOf("《用户协议》")
        val userProtocolEnd = userProtocolStart + "《用户协议》".length
        val privacyPolicyStart = text.indexOf("《隐私政策》")
        val privacyPolicyEnd = privacyPolicyStart + "《隐私政策》".length

        // 点击事件和样式
        val clickableSpanUserProtocol = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl("https://www.mi.com/")
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = Color.parseColor("#2196F3") // 蓝色
                ds.isUnderlineText = false // 取消下划线
            }
        }
        val clickableSpanPrivacyPolicy = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl("https://www.xiaomiev.com/")
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = Color.parseColor("#2196F3")
                ds.isUnderlineText = false
            }
        }

        spannableString.setSpan(clickableSpanUserProtocol, userProtocolStart, userProtocolEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpanPrivacyPolicy, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvPrivacy.text = spannableString
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()
        tvPrivacy.highlightColor = Color.TRANSPARENT // 点击时无背景色

        dimOverlay.visibility = View.VISIBLE
        privacyPopup.visibility = View.VISIBLE

        dimOverlay.setOnClickListener {
            finish()
        }

        btnAgree.setOnClickListener {
            prefs.edit().putBoolean("isFirstLaunch", false).apply()
            startMainActivity()
        }

        btnDisagree.setOnClickListener {
            finish()
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse(url)
        startActivity(intent)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
