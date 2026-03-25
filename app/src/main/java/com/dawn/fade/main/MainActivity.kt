package com.dawn.fade.main

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.dawn.fade.BaseActivity
import com.dawn.fade.R
import com.dawn.fade.main.article.ArticleFragment
import com.dawn.fade.main.dynamic.DynamicFragment
import com.dawn.fade.main.home.HomeFragment
import com.dawn.fade.main.mine.MineFragment
import com.dawn.fade.main.navigation.MainTabItem
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 主页面，负责承载底部四个 Tab 与对应 Fragment 的切换逻辑。
 */
class MainActivity : BaseActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentTabId: Int = R.id.menu_home

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.mainBottomNavigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            switchToTab(MainTabItem.fromMenuItemId(item.itemId))
            true
        }

        val initialTabId = savedInstanceState?.getInt(KEY_SELECTED_TAB_ID) ?: R.id.menu_home
        bottomNavigationView.menu.findItem(initialTabId).isChecked = true
        switchToTab(MainTabItem.fromMenuItemId(initialTabId))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB_ID, currentTabId)
    }

    private fun switchToTab(tabItem: MainTabItem) {
        val fragmentManager = supportFragmentManager
        val targetFragment = fragmentManager.findFragmentByTag(tabItem.tag) ?: createFragment(tabItem)
        val transaction = fragmentManager.beginTransaction()

        fragmentManager.fragments
            .filter { it.id == R.id.mainFragmentContainer && it != targetFragment }
            .forEach(transaction::hide)

        if (targetFragment.isAdded) {
            transaction.show(targetFragment)
        } else {
            transaction.add(R.id.mainFragmentContainer, targetFragment, tabItem.tag)
        }

        transaction.commit()
        currentTabId = tabItem.menuItemId
        title = getString(tabItem.titleRes)
        applyTabSystemBarStyle(tabItem)
    }

    private fun createFragment(tabItem: MainTabItem): Fragment {
        return when (tabItem) {
            MainTabItem.Home -> HomeFragment()
            MainTabItem.Article -> ArticleFragment()
            MainTabItem.Dynamic -> DynamicFragment()
            MainTabItem.Mine -> MineFragment()
        }
    }

    private fun applyTabSystemBarStyle(tabItem: MainTabItem) {
        val colorRes = when (tabItem) {
            MainTabItem.Mine -> R.color.mine_page_start
            else -> R.color.screen_background
        }
        updateSystemBarColor(colorRes)
    }

    private companion object {
        const val KEY_SELECTED_TAB_ID = "selected_tab_id"
    }
}
