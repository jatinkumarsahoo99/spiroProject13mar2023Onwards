package com.safey.lungmonitoring.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


object PatientDbMigration {

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            /* database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, `name` TEXT, PRIMARY KEY(`id`))")*/
        }
    }


}
