package com.kavinraj.cse535individualassignment1;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class databaseclass extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "raj.db";
    public static final String TABLE_NAME = "PATIENTS_TABLE";
    public static final String COL_1 = "SERIAL_NO";
    public static final String COL_2 = "HEART_RATE";
    public static final String COL_3 = "RESPIRATORY_RATE";
    public static final String COL_4 = "Nausea";
    public static final String COL_5 = "Headache";
    public static final String COL_6 = "Diarrhea";
    public static final String COL_7 = "Soar_throat";
    public static final String COL_8 = "Fever";
    public static final String COL_9 = "Muscle_Ache";
    public static final String COL_10 = "Loss_of_smell_or_taste";
    public static final String COL_11 = "cough";
    public static final String COL_12 = "Shortness_of_breath";
    public static final String COL_13 = "Feeling_tired";


    public databaseclass(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);


    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_NAME +
                "(SERIAL_NO INTEGER PRIMARY KEY AUTOINCREMENT,HEART_RATE REAL ,RESPIRATORY_RATE REAL,Nausea REAL,Headache REAL,Diarrhea REAL,Soar_throat REAL,Fever REAL,Muscle_Ache REAL,Loss_of_smell_or_taste REAL,cough REAL,Shortness_of_breath REAL,Feeling_tired REAL)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(int s_no,float heartrate,float resprate,float Nausea, float Headache, float Diarrhea, float Soar_throat, float Fever, float Muscle_Ache, float Loss_of_smell_or_taste, float cough, float Shortness_of_breath, float Feeling_tired,boolean update) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_1,s_no);
        if(heartrate != 0)
        {
            contentValues.put(COL_2, heartrate);
        }
        if(resprate != 0)
        {
            contentValues.put(COL_3,resprate);
        }

            contentValues.put(COL_4, Nausea);



            contentValues.put(COL_5, Headache);



            contentValues.put(COL_6, Diarrhea);



            contentValues.put(COL_7, Soar_throat);


            contentValues.put(COL_8, Fever);



            contentValues.put(COL_9, Muscle_Ache);



            contentValues.put(COL_10, Loss_of_smell_or_taste);



            contentValues.put(COL_11, cough);


            contentValues.put(COL_12, Shortness_of_breath);



            contentValues.put(COL_13, Feeling_tired);



        long result;

        if(update)
        {


            result = sqLiteDatabase.update(TABLE_NAME, contentValues, "SERIAL_NO = ?", new String[]{"1"});
        }
        else
        {
            result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        }


        if (result == -1)
            return false;
        else
            return true;
    }
}











