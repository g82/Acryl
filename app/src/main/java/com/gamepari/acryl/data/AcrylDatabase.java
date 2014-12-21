package com.gamepari.acryl.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gamepari on 12/21/14.
 */
public class AcrylDatabase extends SQLiteOpenHelper {

    //modify this path.
    public static final String TSV_FILE_PATH = "we love acryl/test.tsv";
    public static final String DB_NAME = "acryl.db";

    private static final String TABLE_NAME = "tb_acryl";

    private String TAG = this.getClass().getSimpleName();

    public AcrylDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //sample : 중랑구,53,2419,서울시 중랑구 면목2동 124-15,면목2동,열매상상어린이공원 놀이터,"₩5,000"

        String sqlQuery = "CREATE TABLE tb_acryl (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tag_id INTEGER," +
                "inst_id INTEGER," +
                "address1 TEXT," +
                "address2 TEXT," +
                "address_full TEXT," +
                "inst_name TEXT," +
                "pay TEXT," +
                "checked TEXT" +
                ");";

        sqLiteDatabase.execSQL(sqlQuery);

        List<PlaygroundModel> listResult = null;

        TSVReader tsvReader = new TSVReader(Environment.getExternalStorageDirectory().getPath() + "/" + TSV_FILE_PATH);

        try {
            listResult = tsvReader.runParse();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            return;
        }

        if (listResult != null && listResult.size() > 0) {

            for (PlaygroundModel pObject : listResult) {

                ContentValues values = new ContentValues();
                values.put("tag_id", pObject.getTag_num());
                values.put("inst_id", pObject.getInst_num());
                values.put("address_full", pObject.getFullAddress());
                values.put("inst_name", pObject.getInstName());
                values.put("address1", pObject.getAddress1());
                values.put("address2", pObject.getAddress2());
                values.put("pay", pObject.getPay());
                values.put("checked", 0);

                sqLiteDatabase.insertOrThrow(TABLE_NAME, null, values);
            }
        }
    }


    public List<PlaygroundModel> getListPlayGround() {

        List<PlaygroundModel> listPlaygroundModel = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query("tb_acryl", null, null, null, null, null, null);

        PlaygroundModel pObject = null;

        while (cursor.moveToNext()) {

            pObject = new PlaygroundModel();
            listPlaygroundModel.add(pObject);

            pObject.setTag_num(cursor.getInt(1));
            pObject.setInst_num(cursor.getInt(2));
            pObject.setAddress1(cursor.getString(3));
            pObject.setAddress2(cursor.getString(4));
            pObject.setFullAddress(cursor.getString(5));
            pObject.setInstName(cursor.getString(6));
            pObject.setPay(cursor.getString(7));
            pObject.setChecked(cursor.getInt(8));
        }

        sqLiteDatabase.close();

        return listPlaygroundModel;

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }


    public boolean updatePlayGround(PlaygroundModel pObject) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("tag_id", pObject.getTag_num());
        values.put("checked", (pObject.isChecked()) ? 1 : 0);

        int affectedRows = database.update(TABLE_NAME, values,
                "tag_id = ?", new String[]{String.valueOf(pObject.getTag_num())});

        database.close();

        return (affectedRows == 1) ? true : false;
    }
}
