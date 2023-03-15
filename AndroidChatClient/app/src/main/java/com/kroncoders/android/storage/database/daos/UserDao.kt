package com.kroncoders.android.storage.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kroncoders.android.storage.database.entities.EntityUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg entityUser: EntityUser)

    @Query("select count(*) from users where userId= :userId")
    fun countUserWithId(userId: Long): Int

    @Query("select * from users")
    fun getAllUsers(): Flow<List<EntityUser>>
}