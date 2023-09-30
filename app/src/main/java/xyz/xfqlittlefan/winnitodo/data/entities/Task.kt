package xyz.xfqlittlefan.winnitodo.data.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity
data class Task(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val order: Int = 0,
    val title: String,
    val description: String,
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY `order` ASC")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE id = :id LIMIT 1")
    fun getById(id: UUID): Flow<Task>

    @Insert
    fun insert(task: Task)

    @Update
    fun update(task: Task)

    @Delete
    @Deprecated("Use `AppDatabase.deleteTask()` instead.")
    fun deleteDirectly(task: Task)
}
