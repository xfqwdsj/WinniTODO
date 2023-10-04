package xyz.xfqlittlefan.winnitodo.ui.viewModels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.xfqlittlefan.winnitodo.ui.AppViewModel
import java.time.OffsetDateTime

class TaskDetailsPageViewModel(private val appViewModel: AppViewModel) : ViewModel() {
    var isReady by mutableStateOf(appViewModel.currentlyViewingDetailsTaskId == null)
        private set

    var title by mutableStateOf("")
    var description by mutableStateOf("")

    fun updateTask() = appViewModel.updateTask(title, description)

    val doneTasks
        @Composable get() = appViewModel.currentlyViewingDetailsTaskId?.let {
            appViewModel.getDoneTasks(it)
        }?.value ?: emptyList()

    fun navigateToDate(date: OffsetDateTime) {
        appViewModel.navigateToDate(date)
        appViewModel.checkAndPop()
    }

    init {
        val id = appViewModel.currentlyViewingDetailsTaskId
        if (id != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val task = appViewModel.tasks.firstOrNull { it.id == id }
                if (task != null) {
                    withContext(Dispatchers.Main) {
                        title = task.title
                        description = task.description
                        isReady = true
                    }
                }
            }
        }
    }
}
