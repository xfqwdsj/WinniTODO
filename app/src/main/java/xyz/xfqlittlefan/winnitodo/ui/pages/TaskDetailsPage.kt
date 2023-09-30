package xyz.xfqlittlefan.winnitodo.ui.pages

import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.xfqlittlefan.winnitodo.R
import xyz.xfqlittlefan.winnitodo.data.entities.Task
import xyz.xfqlittlefan.winnitodo.ui.AppRoutes
import xyz.xfqlittlefan.winnitodo.ui.LocalAppDatabase
import xyz.xfqlittlefan.winnitodo.ui.LocalNavController
import java.util.UUID

internal fun AppRoutes.taskDetails(id: String) = "taskDetails$id"
fun AppRoutes.taskDetails(id: UUID? = null) = taskDetails("/$id")
val AppRoutes.taskDetails get() = taskDetails("/{${AppRoutes.taskDetailsArgId}}")
val AppRoutes.taskDetailsArgId get() = "id"
val AppRoutes.taskDetailsArgs
    get() = listOf(
        navArgument(AppRoutes.taskDetailsArgId) {
            type = NavType.StringType
            nullable = true
        },
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsPage(id: UUID? = null) {
    val scope = rememberCoroutineScope()
    val database = LocalAppDatabase.current
    val taskDao = database.taskDao()
    val navController = LocalNavController.current

    var isReady by remember { mutableStateOf(id == null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val done: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            if (id == null) {
                taskDao.insert(
                    Task(
                        title = title, description = description,
                    )
                )
            } else {
                taskDao.update(
                    Task(
                        id = id, title = title, description = description,
                    )
                )
            }
            withContext(Dispatchers.Main) {
                navController.popBackStack()
            }
        }
    }

    if (id != null) {
        SideEffect {
            scope.launch(Dispatchers.IO) {
                taskDao.getById(id).collect {
                    title = it.title
                    description = it.description
                    isReady = true
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(
            connection = scrollBehavior.nestedScrollConnection,
        ),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.title_task_details)) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                        ) {
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
            if (isReady) {
                FloatingActionButton(
                    onClick = done,
                ) {
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
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = isReady,
                label = { Text(stringResource(R.string.label_task_title)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = isReady,
                label = { Text(stringResource(R.string.label_task_description)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { done() },
                ),
            )
        }
    }
}

fun NavGraphBuilder.taskDetailsPage() {
    composable(route = AppRoutes.taskDetails, arguments = AppRoutes.taskDetailsArgs) {
        TaskDetailsPage(id = it.arguments?.getString(AppRoutes.taskDetailsArgId)?.let(UUID::fromString))
    }
}

fun NavController.navigateToTaskDetails(id: UUID? = null) {
    navigate(AppRoutes.taskDetails(id))
}
