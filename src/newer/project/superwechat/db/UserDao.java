package newer.project.superwechat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import newer.project.superwechat.I;
import newer.project.superwechat.bean.User;

public class UserDao extends SQLiteOpenHelper{
    private static final String USER_NAME = "user";

    public UserDao(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "user.db", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "DROP TABLE IF EXISTS " + I.User.TABLE_NAME +
                " CREATE TABLE " + I.User.TABLE_NAME +
                I.User.USER_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                I.User.USER_NAME + " TEXT NOT NULL," +
                I.User.PASSWORD + " TEXT NOT NULL," +
                I.User.NICK + "  TEXT NOT NULL," +
                I.User.UN_READ_MSG_COUNT + "  INTEGER DEFAULT 0);";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addUser(User user){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(I.User.USER_ID, user.getMUserId());
        values.put(I.User.USER_NAME, user.getMUserName());
        values.put(I.User.PASSWORD, user.getMUserPassword());
        values.put(I.User.NICK, user.getMUserNick());
        values.put(I.User.UN_READ_MSG_COUNT, user.getMUserUnreadMsgCount());
        long affect = db.insert(I.User.TABLE_NAME, null, values);
        return affect != -1;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(I.User.USER_ID, user.getMUserId());
        values.put(I.User.USER_NAME, user.getMUserName());
        values.put(I.User.PASSWORD, user.getMUserPassword());
        values.put(I.User.NICK, user.getMUserNick());
        values.put(I.User.UN_READ_MSG_COUNT, user.getMUserUnreadMsgCount());
        int affect = db.update(I.User.TABLE_NAME, values, I.User.USER_NAME + " = ?", new String[]{user.getMUserName()});
        return affect != -1;
    }

    public User findUserByUserName(String userName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(I.User.TABLE_NAME, new String[]{"*"}, I.User.TABLE_NAME + " = ?", new String[]{userName}, null, null, null);
        if (cursor.moveToNext()) {
            User user = new User();
            user.setMUserId(cursor.getInt(cursor.getColumnIndex(I.User.USER_ID)));
            user.setMUserPassword(cursor.getString(cursor.getColumnIndex(I.User.PASSWORD)));
            user.setMUserNick(cursor.getString(cursor.getColumnIndex(I.User.NICK)));
            user.setMUserUnreadMsgCount(cursor.getInt(cursor.getColumnIndex(I.User.UN_READ_MSG_COUNT)));
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }
}
