package com.example.taskpulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao

  //  companion object getDatabase(context: Context): TaskDatabase
    companion object{
        @Volatile
        private var INSTANCE: TaskDatabase? = null
      fun getDatabase(context: Context): TaskDatabase{
          return INSTANCE ?: synchronized(this){
              val instance = Room.databaseBuilder(
                  context = context.applicationContext,
                  TaskDatabase::class.java,
                  "task_database"
              )
              .fallbackToDestructiveMigration(true)
              .build()
              INSTANCE = instance
              instance
          }
      }
    }
}