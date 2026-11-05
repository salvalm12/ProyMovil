package com.example.turnofacil.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.turnofacil.data.local.entity.MediaEntity
import com.example.turnofacil.domain.model.TipoPropietarioMedia
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a archivos multimedia (RF11).
 *
 * Solo modelo y DAO; la captura/selección de archivos se hará en otra fase.
 */
@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaEntity): Long

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getById(id: Long): MediaEntity?

    @Query(
        """
        SELECT * FROM media
        WHERE owner_type = :ownerType AND owner_id = :ownerId
        ORDER BY created_at
        """,
    )
    fun observeByOwner(
        ownerType: TipoPropietarioMedia,
        ownerId: Long,
    ): Flow<List<MediaEntity>>

    @Delete
    suspend fun delete(media: MediaEntity)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
