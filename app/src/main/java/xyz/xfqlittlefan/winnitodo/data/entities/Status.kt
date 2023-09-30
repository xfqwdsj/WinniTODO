package xyz.xfqlittlefan.winnitodo.data.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
@TypeConverters(Converters::class)
data class Status(
    @PrimaryKey val date: OffsetDateTime,
    val doneTasks: DoneTasks
)

class DoneTasks(private val taskIds: List<Long>) : List<Long> by taskIds

@Dao
@TypeConverters(Converters::class)
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

class Converters {
    @TypeConverter
    fun toOffsetDateTime(value: Long?): OffsetDateTime? {
        return value?.let { OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): Long? {
        return date?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toDoneTasks(value: String?): DoneTasks? {
        return value?.let { v -> DoneTasks(v.split(',').map { it.toLong() }) }
    }

    @TypeConverter
    fun fromDoneTasks(doneTasks: DoneTasks?): String? {
        return doneTasks?.joinToString(",")
    }
}
