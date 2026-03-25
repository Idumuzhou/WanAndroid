package com.dawn.fade.main.auth

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dawn.fade.BaseActivity
import com.dawn.fade.R
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.repository.Repository
import com.dawn.fade.data.session.UserSessionStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * 登录页，统一承载登录和注册表单交互逻辑。
 */
class LoginActivity : BaseActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var titleView: TextView
    private lateinit var switchModeView: TextView
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var repasswordInput: TextInputEditText
    private lateinit var repasswordContainer: View
    private lateinit var submitButton: MaterialButton
    private lateinit var loadingView: View

    private val repository = Repository()
    private var currentMode: AuthMode = AuthMode.Login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        toolbar = findViewById(R.id.loginToolbar)
        titleView = findViewById(R.id.textLoginTitle)
        switchModeView = findViewById(R.id.textSwitchAuthMode)
        usernameInput = findViewById(R.id.inputUsername)
        passwordInput = findViewById(R.id.inputPassword)
        repasswordInput = findViewById(R.id.inputRepassword)
        repasswordContainer = findViewById(R.id.repasswordContainer)
        submitButton = findViewById(R.id.buttonSubmitAuth)
        loadingView = findViewById(R.id.loginLoadingView)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        switchModeView.setOnClickListener {
            currentMode = if (currentMode == AuthMode.Login) AuthMode.Register else AuthMode.Login
            renderMode()
        }

        submitButton.setOnClickListener {
            submit()
        }

        renderMode()
    }

    private fun renderMode() {
        val isRegister = currentMode == AuthMode.Register
        toolbar.title = getString(if (isRegister) R.string.auth_register_title else R.string.auth_login_title)
        titleView.text = getString(if (isRegister) R.string.auth_register_title else R.string.auth_login_title)
        repasswordContainer.visibility = if (isRegister) View.VISIBLE else View.GONE
        submitButton.text = getString(if (isRegister) R.string.auth_register_action else R.string.auth_login_action)
        switchModeView.text = getString(if (isRegister) R.string.auth_switch_to_login else R.string.auth_switch_to_register)
    }

    private fun submit() {
        val username = usernameInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString().orEmpty()
        val repassword = repasswordInput.text?.toString().orEmpty()

        if (username.isBlank()) {
            toast(R.string.auth_username_required)
            return
        }

        if (password.isBlank()) {
            toast(R.string.auth_password_required)
            return
        }

        if (currentMode == AuthMode.Register) {
            if (repassword.isBlank()) {
                toast(R.string.auth_repassword_required)
                return
            }
            if (password != repassword) {
                toast(R.string.auth_password_mismatch)
                return
            }
        }

        setLoading(true)
        lifecycleScope.launch {
            val result = if (currentMode == AuthMode.Login) {
                repository.login(username = username, password = password)
            } else {
                repository.register(username = username, password = password, repassword = repassword)
            }

            when (result) {
                is ApiResult.Success -> {
                    UserSessionStore.saveUser(this@LoginActivity, result.data)
                    syncUserInfoAndFinish()
                }
                is ApiResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun syncUserInfoAndFinish() {
        when (val userInfoResult = repository.fetchUserInfo()) {
            is ApiResult.Success -> {
                val previousUser = UserSessionStore.getUser(this)
                UserSessionStore.saveUser(
                    this,
                    userInfoResult.data.toUserData(previousUser)
                )
            }
            is ApiResult.Error -> {
                if (userInfoResult.errorCode == -1001) {
                    UserSessionStore.clear(this)
                }
            }
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
        submitButton.isEnabled = !isLoading
        switchModeView.isEnabled = !isLoading
        usernameInput.isEnabled = !isLoading
        passwordInput.isEnabled = !isLoading
        repasswordInput.isEnabled = !isLoading
    }

    private fun toast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }
}

/**
 * 登录页模式定义，区分登录和注册两种表单状态。
 */
private enum class AuthMode {
    Login,
    Register
}
