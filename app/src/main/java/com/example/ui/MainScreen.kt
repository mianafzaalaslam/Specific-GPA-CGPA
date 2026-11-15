package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CgpaDashboardScreen
import com.example.ui.screens.GpaCalculatorScreen
import com.example.ui.screens.GradingSchemeScreen

@Composable
fun MainScreen(viewModel: GpaViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentTab by rememberSaveable { mutableStateOf(AppTab.GPA) }

  // Return to the primary GPA tab on system Back when viewing secondary tabs
  BackHandler(enabled = currentTab != AppTab.GPA) {
    currentTab = AppTab.GPA
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    if (useNavigationRail) {
      Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
            AppTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = { TabIcon(tab = tab, selected = selected) },
                label = { Text(tab.label) },
                modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}"),
              )
            }
          }
          TabContent(
            currentTab = currentTab,
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToTab = { currentTab = it },
            modifier = Modifier.weight(1f).fillMaxSize(),
          )
        }
      }
    } else {
      Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
          NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            AppTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = { TabIcon(tab = tab, selected = selected) },
                label = { Text(tab.label) },
                modifier = Modifier.testTag("nav_bar_${tab.name.lowercase()}"),
              )
            }
          }
        },
      ) { innerPadding ->
        TabContent(
          currentTab = currentTab,
          uiState = uiState,
          viewModel = viewModel,
          onNavigateToTab = { currentTab = it },
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
      }
    }
  }
}

@Composable
private fun TabIcon(tab: AppTab, selected: Boolean) {
  val icon =
    when (tab) {
      AppTab.GPA -> if (selected) Icons.Rounded.Calculate else Icons.Outlined.Calculate
      AppTab.CGPA -> if (selected) Icons.Rounded.AutoGraph else Icons.Outlined.AutoGraph
      AppTab.SCHEME -> if (selected) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook
    }
  Icon(imageVector = icon, contentDescription = tab.label)
}

@Composable
private fun TabContent(
  currentTab: AppTab,
  uiState: GpaUiState,
  viewModel: GpaViewModel,
  onNavigateToTab: (AppTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (currentTab) {
    AppTab.GPA ->
      GpaCalculatorScreen(
        uiState = uiState,
        onSelectSemester = viewModel::selectSemester,
        onAddSemester = viewModel::addSemester,
        onUpdateSemester = { sem, title, gpa, credits ->
          if (sem.isSummaryOnly) {
            viewModel.updateSummarySemester(sem, title, gpa, credits)
          } else {
            viewModel.renameSemester(sem, title)
          }
        },
        onSaveCourse = viewModel::saveCourse,
        onDeleteCourse = viewModel::deleteCourse,
        modifier = modifier,
      )
    AppTab.CGPA ->
      CgpaDashboardScreen(
        uiState = uiState,
        onOpenSemesterInGpa = { semesterId ->
          viewModel.selectSemester(semesterId)
          onNavigateToTab(AppTab.GPA)
        },
        onAddSemester = viewModel::addSemester,
        onUpdateSemester = { sem, title, gpa, credits ->
          if (sem.isSummaryOnly) {
            viewModel.updateSummarySemester(sem, title, gpa, credits)
          } else {
            viewModel.renameSemester(sem, title)
          }
        },
        onToggleSemesterIncluded = viewModel::toggleSemesterIncluded,
        onDeleteSemester = viewModel::deleteSemester,
        onSavePriorAcademic = viewModel::savePriorAcademic,
        modifier = modifier,
      )
    AppTab.SCHEME -> GradingSchemeScreen(modifier = modifier)
  }
}
