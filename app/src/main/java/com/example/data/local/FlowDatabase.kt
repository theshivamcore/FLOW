package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Habit::class,
        HabitCompletion::class,
        Challenge::class,
        PersonalGoal::class,
        AiDayTask::class,
        AiMemory::class,
        UserFollow::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun goalDao(): GoalDao
    abstract fun aiDao(): AiDao
    abstract fun followDao(): FollowDao

    companion object {
        @Volatile
        private var INSTANCE: FlowDatabase? = null

        fun getDatabase(context: Context): FlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowDatabase::class.java,
                    "flow_habits_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
