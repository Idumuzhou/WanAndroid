package com.dawn.fade.page

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dawn.fade.BaseActivity
import com.dawn.fade.R
import com.dawn.fade.theme.settings.AppThemeMode
import com.dawn.fade.theme.settings.ThemePreferenceStore
import com.dawn.fade.ui.theme.DawnComposeTheme

class SettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DawnComposeTheme {
                ThemeSettingsScreen()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ThemeSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedMode by rememberSaveable {
        mutableStateOf(ThemePreferenceStore.getThemeMode(context))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_mode_title),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Card(
                colors = CardDefaults.cardColors()
            ) {
                AppThemeMode.entries.forEach { mode ->
                    ThemeModeItem(
                        mode = mode,
                        selected = selectedMode == mode,
                        onSelected = {
                            selectedMode = mode
                            ThemePreferenceStore.setThemeMode(context, mode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeItem(
    mode: AppThemeMode,
    selected: Boolean,
    onSelected: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = when (mode) {
                    AppThemeMode.FollowSystem -> stringResource(R.string.theme_mode_follow_system)
                    AppThemeMode.Light -> stringResource(R.string.theme_mode_light)
                    AppThemeMode.Dark -> stringResource(R.string.theme_mode_dark)
                }
            )
        },
        supportingContent = {
            Text(
                text = when (mode) {
                    AppThemeMode.FollowSystem -> stringResource(R.string.theme_mode_follow_system_summary)
                    AppThemeMode.Light -> stringResource(R.string.theme_mode_light_summary)
                    AppThemeMode.Dark -> stringResource(R.string.theme_mode_dark_summary)
                }
            )
        },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = null
            )
        },
        modifier = Modifier.clickable(onClick = onSelected)
    )
}

@Preview(showBackground = true)
@Composable
fun ThemeSettingsScreenPreview() {
    DawnComposeTheme {
        ThemeSettingsScreen()
    }
}
