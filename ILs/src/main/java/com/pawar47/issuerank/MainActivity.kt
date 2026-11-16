package com.pawar47.issuerank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pawar47.issuerank.ui.IosTheme
import com.pawar47.issuerank.ui.IssueRankApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            IosTheme {
                IssueRankApp()
            }
        }
    }
}
