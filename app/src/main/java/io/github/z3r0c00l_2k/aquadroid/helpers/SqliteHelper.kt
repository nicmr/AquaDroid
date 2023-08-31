package io.github.z3r0c00l_2k.aquadroid.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// TODO: use prepared statements for performance and security
// TODO: refactor 'intook'
// TODO: consider passing dates in as actual dates instead of strings
class SqliteHelper(val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    companion object {
        // general values
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Aqua"
        private const val TABLE_STATS = "stats"
        private const val TABLE_CONFIGURED_AMOUNTS = ""

        // specific to stats table
        private const val STATS_KEY_ID = "id"
        private const val STATS_KEY_DATE = "date"
        private const val STATS_KEY_INTOOK = "intook"
        private const val STATS_KEY_TOTAL_INTAKE = "totalintake"

        // specific to configuredAmounts table
//        private const val
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_STATS_TABLE = ("CREATE TABLE " + TABLE_STATS + "("
                + STATS_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + STATS_KEY_DATE + " TEXT UNIQUE,"
                + STATS_KEY_INTOOK + " INT," + STATS_KEY_TOTAL_INTAKE + " INT" + ")")
        db?.execSQL(CREATE_STATS_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS)
        onCreate(db)
    }

    fun addAll(date: String, intook: Int, totalintake: Int): Long {
        if (checkExistance(date) == 0) {
            val values = ContentValues()
            values.put(STATS_KEY_DATE, date)
            values.put(STATS_KEY_INTOOK, intook)
            values.put(STATS_KEY_TOTAL_INTAKE, totalintake)
            val db = this.writableDatabase
            val response = db.insert(TABLE_STATS, null, values)
            db.close()
            return response
        }
        return -1
    }

    fun getIntook(date: String): Int {
        val selectQuery = "SELECT $STATS_KEY_INTOOK FROM $TABLE_STATS WHERE $STATS_KEY_DATE = ?"
        val db = this.readableDatabase
        db.rawQuery(selectQuery, arrayOf(date)).use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndex(STATS_KEY_INTOOK))
            }
        }
        return 0
    }

    fun addIntook(date: String, selectedOption: Int): Int {
        val intook = getIntook(date)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(STATS_KEY_INTOOK, intook + selectedOption)

        val response = db.update(TABLE_STATS, contentValues, "$STATS_KEY_DATE = ?", arrayOf(date))
        db.close()
        return response
    }

    fun checkExistance(date: String): Int {
        val selectQuery = "SELECT $STATS_KEY_INTOOK FROM $TABLE_STATS WHERE $STATS_KEY_DATE = ?"
        val db = this.readableDatabase
        db.rawQuery(selectQuery, arrayOf(date)).use {
            if (it.moveToFirst()) {
                return it.count
            }
        }
        return 0
    }

    fun getAllStats(): Cursor {
        val selectQuery = "SELECT * FROM $TABLE_STATS"
        val db = this.readableDatabase
        return db.rawQuery(selectQuery, null)

    }

    fun updateTotalIntake(date: String, totalintake: Int): Int {
        val intook = getIntook(date)
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(STATS_KEY_TOTAL_INTAKE, totalintake)

        val response = db.update(TABLE_STATS, contentValues, "$STATS_KEY_DATE = ?", arrayOf(date))
        db.close()
        return response
    }

}
