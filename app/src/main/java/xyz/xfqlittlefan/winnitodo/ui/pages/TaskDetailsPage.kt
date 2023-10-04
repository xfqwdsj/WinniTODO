package xyz.xfqlittlefan.winnitodo.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import xyz.xfqlittlefan.winnitodo.R
import xyz.xfqlittlefan.winnitodo.ui.AppRoutes
import xyz.xfqlittlefan.winnitodo.ui.LocalAppViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Suppress("UnusedReceiverParameter")
val AppRoutes.taskDetails get() = "taskDetails"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailsPage() {
    val viewModel = LocalAppViewModel.current
    val pageViewModel = viewModel.taskDetailsPageViewModel()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.title_task_details)) },
                navigationIcon = {
                    if (viewModel.canPop) {
                        IconButton(onClick = viewModel::pop) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_task_details_cancel),
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (viewModel.canPop && pageViewModel.isReady) {
                FloatingActionButton(onClick = pageViewModel::updateTask) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.action_task_details_done),
                    )
                }
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            TextField(
                value = pageViewModel.title,
                onValueChange = pageViewModel::updateTitle,
                modifier = Modifier.fillMaxWidth(),
                enabled = pageViewModel.isReady,
                label = { Text(stringResource(R.string.label_task_title)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                value = pageViewModel.description,
                onValueChange = pageViewModel::updateDescription,
                modifier = Modifier.fillMaxWidth(),
                enabled = pageViewModel.isReady,
                label = { Text(stringResource(R.string.label_task_description)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { pageViewModel.updateTask() },
                ),
            )
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pageViewModel.doneTasks.forEach { doneTask ->
                    AssistChip(
                        onClick = { pageViewModel.navigateToDate(doneTask.date) },
                        label = {
                            Text(
                                doneTask.date.format(
                                    DateTimeFormatter.ofLocalizedDate(
                                        FormatStyle.LONG
                                    )
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

fun NavGraphBuilder.taskDetailsPage() {
    composable(route = AppRoutes.taskDetails) {
        TaskDetailsPage()
    }
}
