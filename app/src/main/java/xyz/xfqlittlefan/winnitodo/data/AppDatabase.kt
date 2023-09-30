package xyz.xfqlittlefan.winnitodo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.xfqlittlefan.winnitodo.data.entities.Status
import xyz.xfqlittlefan.winnitodo.data.entities.StatusDao
import xyz.xfqlittlefan.winnitodo.data.entities.Task
import xyz.xfqlittlefan.winnitodo.data.entities.TaskDao

@Database(entities = [Task::class, Status::class], version = 1, exportSchema = false)
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
}
