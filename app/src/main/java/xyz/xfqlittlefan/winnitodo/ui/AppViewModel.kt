package xyz.xfqlittlefan.winnitodo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.xfqlittlefan.winnitodo.data.AppDatabase
import xyz.xfqlittlefan.winnitodo.data.entities.DoneTask
import xyz.xfqlittlefan.winnitodo.data.entities.Task
import xyz.xfqlittlefan.winnitodo.ui.pages.taskDetails
import xyz.xfqlittlefan.winnitodo.ui.viewModels.TaskDetailsPageViewModel
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class AppViewModel(
    private val database: AppDatabase,
    private val navController: NavController,
) : ViewModel() {
    private val _date = mutableStateOf(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))
    var date: OffsetDateTime
        get() = _date.value
        set(value) {
            _date.value = value.truncatedTo(ChronoUnit.DAYS)
        }

    var currentlyViewingTaskId by mutableStateOf<UUID?>(null)

    private val taskDao = database.taskDao()
    private val statusDao = database.statusDao()

    val tasks by taskDao.getAll().asState(emptyList())

    private fun getDoneTasks(task: Task): State<List<DoneTask>> {
        return statusDao.getByTask(taskId = task.id).asState(emptyList())
    }

    @Composable
    fun rememberDoneTasks(task: Task) = remember { getDoneTasks(task) }

    fun checkTask(doneTasks: List<DoneTask>, task: Task): Boolean {
        return doneTasks.any {
            it.date == date && it.taskId == task.id
        }
    }

    fun updateTask(title: String, description: String) {
        val id = currentlyViewingTaskId
        viewModelScope.launch(context = Dispatchers.IO) {
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
                checkAndPop()
            }
        }
    }

    fun switchTaskStatus(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            statusDao.switchStatus(dateTime = date, taskId = task.id)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteTask(task)
        }
    }

    var canPop by mutableStateOf(navController.previousBackStackEntry != null)
        private set

    private fun updateCanPop() {
        canPop = navController.previousBackStackEntry != null
    }

    fun pop() = navController.popBackStack()
    private fun checkAndPop() = if (canPop) pop() else false

    fun navigateToTaskDetails(taskId: UUID? = null) {
        currentlyViewingTaskId = taskId
        navController.navigate(AppRoutes.taskDetails)
    }

    @Composable
    fun taskDetailsPageViewModel(): TaskDetailsPageViewModel {
        return viewModel(
            factory = viewModelFactory {
                addInitializer(TaskDetailsPageViewModel::class) {
                    TaskDetailsPageViewModel(this@AppViewModel)
                }
            },
        )
    }

    private fun <T> Flow<T>.asState(initialValue: T): State<T> {
        val state = mutableStateOf(initialValue)
        viewModelScope.launch(Dispatchers.IO) {
            collect {
                withContext(Dispatchers.Main) {
                    state.value = it
                }
            }
        }
        return state
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            navController.currentBackStackEntryFlow.collect {
                withContext(Dispatchers.Main) {
                    updateCanPop()
                }
            }
        }
    }
}
