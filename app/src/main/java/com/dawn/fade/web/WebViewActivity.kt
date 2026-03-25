package com.dawn.fade.web

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.dawn.fade.BaseActivity
import com.dawn.fade.R
import com.google.android.material.appbar.MaterialToolbar

/**
 * WebView 页面，统一承载文章详情网页展示逻辑。
 */
class WebViewActivity : BaseActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var webView: WebView
    private lateinit var progressView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        toolbar = findViewById(R.id.webToolbar)
        webView = findViewById(R.id.articleWebView)
        progressView = findViewById(R.id.webLoadingProgress)

        toolbar.title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank {
            getString(R.string.web_title_default)
        }
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPressed()
                }
            }
        )

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return handleUrlOverride(request?.url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return handleUrlOverride(url?.let(Uri::parse))
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressView.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }
        }

        val initialUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (initialUrl.isBlank()) {
            Toast.makeText(this, R.string.web_link_unavailable, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        webView.loadUrl(initialUrl)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private fun handleBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    private fun handleUrlOverride(uri: Uri?): Boolean {
        if (uri == null) {
            return true
        }

        val scheme = uri.scheme.orEmpty().lowercase()
        val host = uri.host.orEmpty().lowercase()
        val isHttpScheme = scheme == "http" || scheme == "https"
        val shouldBlock = scheme in BLOCKED_SCHEMES || BLOCKED_HOST_KEYWORDS.any(host::contains)

        return when {
            shouldBlock -> {
                Toast.makeText(this, R.string.web_blocked_url, Toast.LENGTH_SHORT).show()
                true
            }
            !isHttpScheme -> {
                Toast.makeText(this, R.string.web_blocked_url, Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_URL = "extra_url"
        private val BLOCKED_SCHEMES = setOf("intent", "taobao", "tbopen", "tmall", "alipays")
        private val BLOCKED_HOST_KEYWORDS = listOf("taobao.com", "tmall.com", "tb.cn")

        fun createIntent(context: Context, title: String, url: String): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_URL, url)
            }
        }
    }
}
