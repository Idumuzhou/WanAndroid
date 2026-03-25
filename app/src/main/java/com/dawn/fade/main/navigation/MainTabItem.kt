package com.dawn.fade.main.navigation

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.dawn.fade.R

/**
 * 主页面底部导航配置，集中维护菜单、标题和 Fragment 标签的映射关系。
 */
enum class MainTabItem(
    @IdRes val menuItemId: Int,
    @StringRes val titleRes: Int,
    val tag: String
) {
    Home(R.id.menu_home, R.string.tab_home, "main_tab_home"),
    Article(R.id.menu_article, R.string.tab_article, "main_tab_article"),
    Dynamic(R.id.menu_dynamic, R.string.tab_dynamic, "main_tab_dynamic"),
    Mine(R.id.menu_mine, R.string.tab_mine, "main_tab_mine");

    companion object {
        fun fromMenuItemId(@IdRes menuItemId: Int): MainTabItem {
            return entries.firstOrNull { it.menuItemId == menuItemId }
                ?: throw IllegalArgumentException("Unknown menu item id: $menuItemId")
        }
    }
}
