package com.terminatorssh.terminator.data.local.dao

import androidx.room.*
import com.terminatorssh.terminator.data.local.model.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?

    // note: we intentionally use Replace here
    // because we need to overwrite when we sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun nukeTable()
}