package xyz.xfqlittlefan.winnitodo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.firstOrNull
import xyz.xfqlittlefan.winnitodo.data.entities.DoneTask
import xyz.xfqlittlefan.winnitodo.data.entities.StatusDao
import xyz.xfqlittlefan.winnitodo.data.entities.Task
import xyz.xfqlittlefan.winnitodo.data.entities.TaskDao
import java.util.UUID

@Database(entities = [Task::class, DoneTask::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun statusDao(): StatusDao

    companion object {
        private const val DATABASE_NAME = "winnitodo"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context, AppDatabase::class.java, DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun deleteTask(task: Task) {
        @Suppress("DEPRECATION")
        taskDao().deleteDirectly(task)
        statusDao().getByTask(taskId = task.id).firstOrNull()?.let { doneTasks ->
            doneTasks.forEach { doneTask ->
                statusDao().delete(doneTask)
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = uuid?.let { UUID.fromString(it) }
}
