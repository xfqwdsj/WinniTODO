package xyz.xfqlittlefan.winnitodo.data.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
data class Status(
    @PrimaryKey val date: OffsetDateTime,
    val doneTasks: List<Long>
)

@Dao
interface StatusDao {
    @Query("SELECT * FROM status")
    fun getAll(): Flow<List<Status>>

    @Query("SELECT * FROM status WHERE date = :date")
    fun getByDate(date: OffsetDateTime): Flow<Status>

    @Query("SELECT * FROM status WHERE :taskId IN (doneTasks)")
    fun getByTask(taskId: Long): Flow<List<Status>>

    @Insert
    fun insert(status: Status)

    @Update
    fun update(status: Status)

    @Delete
    fun delete(status: Status)
}
