package xyz.xfqlittlefan.winnitodo.data.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Status(
    val date: OffsetDateTime,
    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    val doneTasks: List<DoneTask> = emptyList()
)

@Entity
@TypeConverters(Converters::class)
data class DoneTask(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val date: OffsetDateTime = OffsetDateTime.now(),
    val taskId: UUID
)

@Dao
@TypeConverters(Converters::class)
interface StatusDao {
    @Transaction
    @Query("SELECT * FROM DoneTask")
    fun getAll(): Flow<List<Status>>

    @Transaction
    @Query("SELECT * FROM DoneTask WHERE date = :dateTime")
    fun getByDate(dateTime: OffsetDateTime): Flow<Status>

    @Query("SELECT * FROM DoneTask WHERE taskId = :taskId")
    fun getByTask(taskId: UUID): Flow<List<DoneTask>>

    @Query("SELECT * FROM DoneTask WHERE date = :dateTime AND taskId = :taskId LIMIT 1")
    fun getByDateAndTask(
        dateTime: OffsetDateTime = OffsetDateTime.now(),
        taskId: UUID
    ): Flow<DoneTask?>

    @Insert
    fun insert(task: DoneTask)

    @Transaction
    fun insert(dateTime: OffsetDateTime = OffsetDateTime.now(), taskId: UUID) {
        insert(DoneTask(date = dateTime, taskId = taskId))
    }

    @Delete
    fun delete(task: DoneTask)

    @Transaction
    suspend fun switchState(dateTime: OffsetDateTime = OffsetDateTime.now(), taskId: UUID) {
        val status = getByDateAndTask(dateTime, taskId).firstOrNull()
        if (status == null) {
            insert(dateTime, taskId)
        } else {
            delete(status)
        }
    }
}

class Converters {
    @TypeConverter
    fun toOffsetDateTime(value: Long?): OffsetDateTime? {
        return value?.let {
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): Long? {
        return date?.truncatedTo(ChronoUnit.DAYS)?.toInstant()?.toEpochMilli()
    }
}
