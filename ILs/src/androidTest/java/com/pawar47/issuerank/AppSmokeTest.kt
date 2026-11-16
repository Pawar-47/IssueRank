package com.pawar47.issuerank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests that run on a device or emulator: navigation, the editor, persistence and
 * search. A record is created through the UI and must be findable afterwards, which proves
 * the write reached the database.
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsTheFivePrimaryTabs() {
        listOf("Rank", "Name", "Issues", "Status", "Resolved").forEach { label ->
            rule.onAllNodesWithText(label).onFirst().assertIsDisplayed()
        }
    }

    @Test
    fun opensSearchFromTheTopBar() {
        rule.onNodeWithContentDescription("Search").performClick()
        rule.onAllNodesWithText("Cancel").onFirst().assertIsDisplayed()
    }

    @Test
    fun createsARecordAndFindsItThroughSearch() {
        val name = "UI test record " + System.currentTimeMillis()

        rule.onNodeWithContentDescription("Add record").performClick()
        rule.onAllNodesWithText("New Record").onFirst().assertIsDisplayed()
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(name)
        rule.onNodeWithText("Save").performClick()

        rule.onAllNodesWithText("Rank").onFirst().assertIsDisplayed()
        rule.onNodeWithContentDescription("Search").performClick()
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(name)

        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText("1 result").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
