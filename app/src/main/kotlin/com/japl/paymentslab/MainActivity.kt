package com.japl.paymentslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.japl.paymentslab.ui.LabApp
import com.japl.paymentslab.ui.PaymentsLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaymentsLabTheme {
                LabApp()
            }
        }
    }
}
