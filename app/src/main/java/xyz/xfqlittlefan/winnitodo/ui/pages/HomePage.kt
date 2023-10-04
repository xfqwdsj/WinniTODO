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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import xyz.xfqlittlefan.winnitodo.R
import xyz.xfqlittlefan.winnitodo.data.entities.Task
import xyz.xfqlittlefan.winnitodo.ui.AppRoutes
import xyz.xfqlittlefan.winnitodo.ui.LocalAppViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

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
                title = { Text(viewModel.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = viewModel::navigateToPreviousDay) {
                        Icon(
                            imageVector = Icons.Default.NavigateBefore,
                            contentDescription = stringResource(R.string.action_date_previous_navigate),
                        )
                    }
                    IconButton(
                        onClick = viewModel::navigateToNextDay,
                        enabled = viewModel.canNavigateToNextDay,
                    ) {
                        Icon(
                            imageVector = Icons.Default.NavigateNext,
                            contentDescription = stringResource(R.string.action_date_next_navigate),
                        )
                    }
                    IconButton(onClick = viewModel::navigateToToday) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = stringResource(R.string.action_date_today_navigate),
                        )
                    }
                    IconButton(onClick = viewModel::showDatePicker) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.action_date_picker_show),
                        )
                    }
                },
            )
        },
        snackbarHost = {
            viewModel.SnackbarHost()
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
                val context = LocalContext.current
                val doneTasks by viewModel.getDoneTasks(task.id)

                TaskCard(
                    task = task,
                    done = viewModel.checkTask(doneTasks, task.id),
                    count = doneTasks.size,
                    onSwitch = { viewModel.switchTaskStatus(task.id) },
                    onDelete = { viewModel.deleteTask(context, task) },
                )
            }
        }
    }

    viewModel.currentlyShowingStatisticsTaskId?.let { id ->
        val doneTasks by viewModel.getDoneTasks(id)

        AlertDialog(
            onDismissRequest = viewModel::hideStatistics,
            confirmButton = {
                TextButton(onClick = viewModel::hideStatistics) {
                    Text(stringResource(R.string.action_task_statistics_close))
                }
            },
            title = {
                Text(stringResource(R.string.label_statistics_title))
            },
            text = {
                Text(
                    "${
                        stringResource(
                            R.string.text_task_details_completed_times,
                            stringResource(R.string.text_task_details_completed_times_placeholder_total),
                            doneTasks.size,
                        )
                    }\n${
                        stringResource(
                            R.string.text_task_details_completed_times,
                            viewModel.date.month.getDisplayName(
                                TextStyle.FULL, Locale.getDefault()
                            ),
                            doneTasks.count { it.date.year == viewModel.date.year && it.date.month == viewModel.date.month },
                        )
                    }\n${
                        stringResource(
                            R.string.text_task_details_completed_times,
                            viewModel.date.year,
                            doneTasks.count { it.date.year == viewModel.date.year },
                        )
                    }"
                )
            },
        )
    }

    if (viewModel.showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = viewModel::hideDatePicker,
            confirmButton = {
                TextButton(
                    onClick = { datePickerState.selectedDateMillis?.let { viewModel.selectDate(it) } },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(stringResource(R.string.action_date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDatePicker) {
                    Text(stringResource(R.string.action_date_picker_cancel))
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = { viewModel.canNavigateToDate(viewModel.transformUTCDate(it)) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task, done: Boolean, count: Int, onSwitch: () -> Unit, onDelete: () -> Unit
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
                    onClick = onSwitch,
                )
                .padding(16.dp),
        ) {
            val noTitleText = stringResource(R.string.text_task_no_title)
            val noDescriptionText = stringResource(R.string.text_task_no_description)
            Text(
                text = task.title.ifEmpty { noTitleText },
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(task.description.ifEmpty { noDescriptionText })
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(
                    R.string.text_task_details_completed_times,
                    stringResource(R.string.text_task_details_completed_times_placeholder_total),
                    count
                )
            )
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
                text = { Text(stringResource(R.string.action_task_statistics_show)) },
                onClick = {
                    showMenu = false
                    viewModel.showStatistics(task.id)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = stringResource(R.string.action_task_statistics_show),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_task_delete)) },
                onClick = {
                    showMenu = false
                    onDelete()
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
