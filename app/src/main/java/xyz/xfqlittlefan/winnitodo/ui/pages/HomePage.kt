package xyz.xfqlittlefan.winnitodo.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import xyz.xfqlittlefan.winnitodo.R
import xyz.xfqlittlefan.winnitodo.data.entities.Task
import xyz.xfqlittlefan.winnitodo.ui.AppRoutes
import xyz.xfqlittlefan.winnitodo.ui.LocalAppViewModel

@Suppress("UnusedReceiverParameter")
val AppRoutes.home get() = "home"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    val viewModel = LocalAppViewModel.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(
            connection = scrollBehavior.nestedScrollConnection,
        ),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::navigateToTaskDetails) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_task_add)
                )
            }
        },
    ) { contentPadding ->
        val layoutDirection = LocalLayoutDirection.current
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(180.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(viewModel.tasks) { task ->
                val doneTasks by viewModel.rememberDoneTasks(task)

                TaskCard(
                    task = task,
                    done = viewModel.checkTask(doneTasks, task),
                    count = doneTasks.size,
                    onSwitch = viewModel::switchTaskStatus,
                    onDelete = viewModel::deleteTask,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    done: Boolean,
    count: Int,
    onSwitch: (task: Task) -> Unit,
    onDelete: (task: Task) -> Unit
) {
    val viewModel = LocalAppViewModel.current

    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = if (done) CardDefaults.shape else CardDefaults.outlinedShape,
        colors = if (done) CardDefaults.cardColors() else CardDefaults.outlinedCardColors(),
        elevation = if (done) CardDefaults.cardElevation() else CardDefaults.outlinedCardElevation(),
        border = if (done) null else CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onLongClick = { showMenu = true },
                    onClick = { onSwitch(task) },
                )
                .padding(16.dp),
        ) {
            Text(text = task.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(task.description)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.text_task_details_completed_times, count))
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_task_details_view)) },
                onClick = {
                    showMenu = false
                    viewModel.navigateToTaskDetails(task.id)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_task_details_view),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_task_delete)) },
                onClick = {
                    showMenu = false
                    onDelete(task)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_task_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

fun NavGraphBuilder.homePage() {
    composable(route = AppRoutes.home) { HomePage() }
}
