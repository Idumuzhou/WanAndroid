package com.dawn.fade.main.mine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dawn.fade.R
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.network.cookie.CookieStore
import com.dawn.fade.data.repository.Repository
import com.dawn.fade.data.session.UserSessionStore
import com.dawn.fade.main.auth.LoginActivity
import com.dawn.fade.page.SettingActivity
import kotlinx.coroutines.launch

/**
 * 我的 Fragment，负责展示登录态、设置入口和退出登录能力。
 */
class MineFragment : Fragment(R.layout.fragment_mine) {
    private lateinit var avatarView: TextView
    private lateinit var usernameView: TextView
    private lateinit var loginStatusView: TextView
    private lateinit var coinValueView: TextView
    private lateinit var levelValueView: TextView
    private lateinit var rankValueView: TextView
    private lateinit var logoutButton: Button
    private lateinit var profileCardView: View
    private val repository = Repository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarView = view.findViewById(R.id.textAvatar)
        profileCardView = view.findViewById(R.id.profileCardView)
        usernameView = view.findViewById(R.id.textUsername)
        loginStatusView = view.findViewById(R.id.textLoginStatus)
        coinValueView = view.findViewById(R.id.textCoinValue)
        levelValueView = view.findViewById(R.id.textLevelValue)
        rankValueView = view.findViewById(R.id.textRankValue)
        logoutButton = view.findViewById(R.id.buttonLogout)

        view.findViewById<Button>(R.id.buttonOpenSettings).setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }

        profileCardView.setOnClickListener { handleAuthAction() }
        avatarView.setOnClickListener { handleAuthAction() }
        usernameView.setOnClickListener { handleAuthAction() }
        logoutButton.setOnClickListener { logout() }
    }

    override fun onResume() {
        super.onResume()
        renderLoginState()
    }

    private fun handleAuthAction() {
        if (UserSessionStore.isLoggedIn(requireContext())) {
            return
        }
        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }

    private fun renderLoginState() {
        val user = UserSessionStore.getUser(requireContext())
        if (user == null) {
            avatarView.text = getString(R.string.mine_avatar_guest)
            usernameView.text = getString(R.string.mine_guest_username)
            loginStatusView.text = getString(R.string.mine_guest_message)
            coinValueView.text = getString(R.string.mine_stat_placeholder)
            levelValueView.text = getString(R.string.mine_stat_placeholder)
            rankValueView.text = getString(R.string.mine_stat_placeholder)
            logoutButton.visibility = View.GONE
            return
        }

        val displayName = user.displayName.ifBlank { user.username }
        avatarView.text = displayName.take(1).uppercase()
        usernameView.text = displayName
        loginStatusView.text = getString(R.string.mine_logged_in_message)
        coinValueView.text = user.coinCount.toString()
        levelValueView.text = user.level.toString()
        rankValueView.text = user.rank.ifBlank { getString(R.string.mine_stat_placeholder) }
        logoutButton.visibility = View.VISIBLE
    }

    private fun logout() {
        lifecycleScope.launch {
            when (val result = repository.logout()) {
                is ApiResult.Success -> {
                    UserSessionStore.clear(requireContext())
                    CookieStore.clear()
                    renderLoginState()
                }
                is ApiResult.Error -> {
                    android.widget.Toast.makeText(requireContext(), result.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
