/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.kibriya.aura.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kibriya.aura.data.local.dao.PlaylistDao
import com.kibriya.aura.data.local.dao.SongDao
import com.kibriya.aura.data.local.entities.AlbumEntity
import com.kibriya.aura.data.local.entities.ArtistEntity
import com.kibriya.aura.data.local.entities.PlaylistEntity
import com.kibriya.aura.data.local.entities.PlaylistSongCrossRef
import com.kibriya.aura.data.local.entities.SongEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "aura_database"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAuraDatabase(
        @ApplicationContext context: Context
    ): AuraDatabase = Room.databaseBuilder(
        context,
        AuraDatabase::class.java,
        AuraDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideSongDao(database: AuraDatabase): SongDao = database.songDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: AuraDatabase): PlaylistDao = database.playlistDao()
}