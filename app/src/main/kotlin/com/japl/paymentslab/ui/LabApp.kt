package com.japl.paymentslab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private val TABS = listOf("Trama serial", "TLV (EMV)", "PAN")

@Composable
fun LabApp() {
    var selected by rememberSaveable { mutableIntStateOf(0) }

    // The parser and its log live here so they survive tab switches.
    val frameLab = remember { FrameLabState() }

    // Keeps the text typed in each tab when switching between them.
    val saveableStateHolder = rememberSaveableStateHolder()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(Modifier.padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selected) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = selected == index,
                        onClick = { selected = index },
                        text = { Text(title) },
                    )
                }
            }
            saveableStateHolder.SaveableStateProvider(selected) {
                when (selected) {
                    0 -> FrameScreen(frameLab)
                    1 -> TlvScreen()
                    else -> PanScreen()
                }
            }
        }
    }
}
