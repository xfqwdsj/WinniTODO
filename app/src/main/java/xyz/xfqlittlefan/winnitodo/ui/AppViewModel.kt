package xyz.xfqlittlefan.winnitodo.ui

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.xfqlittlefan.winnitodo.R
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
    private val databaseScope = CoroutineScope(Job())

    private val _date = MutableStateFlow(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))


    /**
     * 当前正在查看的日期。
     */
    var date by _date.asMutableState(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)) {
        it.truncatedTo(ChronoUnit.DAYS)
    }

    private val snackbarHostState = SnackbarHostState()

    /**
     * 提供 [SnackbarHost]。
     */
    @Composable
    fun SnackbarHost() = SnackbarHost(hostState = snackbarHostState)

    /**
     * 当前正在查看的 [Task] 的 ID。
     */
    var currentlyViewingTaskId by mutableStateOf<UUID?>(null)
        private set

    private val taskDao = database.taskDao()
    private val statusDao = database.statusDao()

    private val deletingTasks = MutableStateFlow<List<Task>>(emptyList())

    /**
     * 所有 [Task]。
     */
    val tasks by taskDao.getAll().combine(deletingTasks) { tasks, deletingTasks ->
        tasks.filterNot { deletingTasks.contains(it) }
    }.asState(emptyList())

//    inline fun getDoneTasks(task: Task) = tasks.first { it.id == task.id }.

//    private fun getDoneTasksFlow(task: Task): Flow<List<DoneTask>> {
//        return statusDao.getByTask(task.id)
//    }
//
//    private fun getDoneTasks(task: Task): State<List<DoneTask>> {
//        return statusDao.getByTask(task.id).asState(emptyList())
//    }

    /**
     * 获取 [task] 的已完成列表。
     *
     * @param task 要获取已完成列表的 [Task]。
     *
     * @return 已使用 [remember] 记住的 [task] 的已完成列表。
     */
    @Composable
    fun getDoneTasks(task: Task) = statusDao.getByTask(task.id).collectAsState(emptyList())

    /**
     * 检查 [task] 是否已完成。
     *
     * @param doneTasks [DoneTask] 列表。
     * @param task 要检查的 [Task]。
     * @return 如果 [task] 已完成则返回 `true`，否则返回 `false`。
     */
    fun checkTask(doneTasks: List<DoneTask>, task: Task): Boolean {
        return doneTasks.any {
            it.date == date && it.taskId == task.id
        }
    }

    /**
     * 更新 [currentlyViewingTaskId] 对应的 [Task]。
     *
     * 如果 [currentlyViewingTaskId] 为 `null`，则插入一个新的 [Task]。
     *
     * @param title [Task] 的标题。
     * @param description [Task] 的描述。
     */
    fun updateTask(title: String, description: String) {
        val id = currentlyViewingTaskId
        databaseScope.launch(Dispatchers.IO) {
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

    /**
     * 切换 [task] 的状态。
     *
     * @param task 要切换状态的 [Task]。
     */
    fun switchTaskStatus(task: Task) {
        databaseScope.launch(Dispatchers.IO) {
            statusDao.switchStatus(dateTime = date, taskId = task.id)
        }
    }

    private suspend fun deleteTask(task: Task) {
        database.deleteTask(task)
        deletingTasks.emit(deletingTasks.value - task)
    }

    /**
     * 删除 [task]。
     *
     * @param context 用于获取字符串资源的 [Context]。
     * @param task 要删除的 [Task]。
     */
    fun deleteTask(context: Context, task: Task) {
        databaseScope.launch(Dispatchers.IO) {
            deletingTasks.emit(deletingTasks.value + task)
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.text_task_deleted),
                actionLabel = context.getString(R.string.action_task_delection_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            when (result) {
                SnackbarResult.Dismissed -> deleteTask(task)
                SnackbarResult.ActionPerformed -> deletingTasks.emit(deletingTasks.value - task)
            }
        }
    }

    /**
     * 判断是否可以返回上一个页面。
     */
    var canPop by mutableStateOf(navController.previousBackStackEntry != null)
        private set

    private fun updateCanPop() {
        canPop = navController.previousBackStackEntry != null
    }

    /**
     * 返回上一个页面。
     *
     * @return 如果可以返回上一个页面则返回 `true`，否则返回 `false`。
     */
    fun pop() = navController.popBackStack()

    /**
     * 检查是否可以返回上一个页面，如果可以才返回。
     *
     * @return 如果可以返回上一个页面则返回 `true`，否则返回 `false`。
     */
    private fun checkAndPop() = if (canPop) pop() else false

    fun navigateToTaskDetails(taskId: UUID? = null) {
        currentlyViewingTaskId = taskId
        navController.navigate(AppRoutes.taskDetails)
    }

    /**
     * 提供 [TaskDetailsPageViewModel]。
     */
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

    /**
     * 将 [Flow] 转换为 [State]。作为函数的返回值在 [Composable] 函数中使用时需要使用 [remember]。
     *
     * @param initialValue [State] 的初始值。
     *
     * @return 转换后的 [State]。
     */
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

    private fun <T> MutableStateFlow<T>.asMutableState(
        initialValue: T,
        setValue: (value: T) -> T
    ): MutableState<T> {
        val state = mutableStateOf(initialValue)
        viewModelScope.launch(Dispatchers.IO) {
            collect {
                withContext(Dispatchers.Main) {
                    state.value = it
                }
            }
        }
        return object : MutableState<T> by state {
            override var value: T
                get() = state.value
                set(value) {
                    viewModelScope.launch {
                        emit(setValue(value))
                    }
                }
        }
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

    /**
     * 该 [ViewModel] 的清理方法。
     */
    override fun onCleared() {
        databaseScope.launch(Dispatchers.IO) {
            deletingTasks.value.forEach {
                deleteTask(it)
            }
        }
        try {
            val job = databaseScope.coroutineContext.job
            job.invokeOnCompletion {
                clean()
            }
        } catch (e: Exception) {
            clean()
        }
    }

    private fun clean() {
        database.close()
        databaseScope.cancel()
    }
}
