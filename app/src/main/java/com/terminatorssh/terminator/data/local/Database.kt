package com.terminatorssh.terminator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.local.model.BlobEntity
import com.terminatorssh.terminator.data.local.model.UserEntity

@Database(entities = [UserEntity::class, BlobEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun blobDao(): BlobDao
}