package com.pos.flightpos.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.pos.flightpos.objects.User;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class POSDBHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "POS_LOCAL.db";

    public POSDBHandler(Context context){
        super(context, DATABASE_NAME , null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS UserDetails (Username VARCHAR,Password VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertUserData(Context context){

        SQLiteDatabase db = this.getWritableDatabase();
        for(User user : readXML(context)) {
            db.execSQL("INSERT INTO UserDetails VALUES('"+user.getUserName()+"','"+user.getPassword()+"');");
        }
    }

    public boolean isLoginSuccess(String userName, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery("select * from UserDetails where " +
                    "Username='" + userName + "' and Password='" + password + "'", null);
            return res.getCount() > 0;
        }
        catch (Exception e){
            return false;
        }
    }

    private List<User> readXML(Context context){
        List<User> itemList = new ArrayList<>();
        try {
            Document doc = getXMLDoc(context);
            if(doc != null) {
                Element element = doc.getDocumentElement();
                element.normalize();
                NodeList nList = doc.getElementsByTagName("user");
                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element2 = (Element) node;
                        User item = new User();
                        item.setUserName(getValue("userName", element2));
                        item.setPassword(getValue("password", element2));
                        itemList.add(item);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return itemList;
    }

    private Document getXMLDoc(Context context) {
        try{
            File f = new File(context.getFilesDir(),"users.xml");
            InputStream is = new FileInputStream(f);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(is);
        }catch (Exception e){
            return null;
        }
    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
}
