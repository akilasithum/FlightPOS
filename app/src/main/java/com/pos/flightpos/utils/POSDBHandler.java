package com.pos.flightpos.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pos.flightpos.InventoryReportActivity;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.User;
import com.pos.flightpos.objects.XMLMapper.Equipment;
import com.pos.flightpos.objects.XMLMapper.Item;
import com.pos.flightpos.objects.XMLMapper.Items;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.objects.XMLMapper.KitNumber;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class POSDBHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "POS_LOCAL.db";

    public POSDBHandler(Context context){
        super(context, DATABASE_NAME , null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Users (Username VARCHAR,Password VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS flights (flightName VARCHAR,flightFrom VARCHAR,flightTo VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS items (itemNo VARCHAR,itemName VARCHAR,itemHHC VARCHAR,category VARCHAR," +
                "catCode VARCHAR,catlogNo VARCHAR,price VARCHAR," +
                "paxDiscPrice VARCHAR,staffDiscPrice VARCHAR,delist VARCHAR,dfsrOrder VARCHAR,serviceType VARCHAR,scPrice VARCHAR," +
                "baseCurrency VARCHAR,basePrice VARCHAR,secondCurrency VARCHAR,secondPrice VARCHAR,activeDate VARCHAR,weight VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS KITList (equipmentNo VARCHAR,itemNo VARCHAR," +
                "itemDescription VARCHAR,quantity VARCHAR,drawer VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS KITNumberList (kitCode VARCHAR,kitDesc VARCHAR," +
                "serviceType VARCHAR,activeDate VARCHAR,noOfEq VARCHAR,noOfSeals VARCHAR,equipment VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS equipmentType (equipmentNo VARCHAR,equipmentDesc VARCHAR," +
                "equipmentType VARCHAR,drawerPrefix VARCHAR,noOfDrawers VARCHAR,kitCode VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS drawerValidation (equipmentNo VARCHAR," +
                "drawer VARCHAR,isValidated VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS dailySales (orderNumber VARCHAR,itemNo VARCHAR," +
                "serviceType VARCHAR,quantity VARCHAR,totalPrice VARCHAR,buyerType VARCHAR);");
    }

    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Users", null);
        if(res.getCount() > 0) {
            db.execSQL("delete from Users");
            db.execSQL("delete from flights");
            db.execSQL("delete from KITList");
            db.execSQL("delete from KITNumberList");
            db.execSQL("delete from items");
            db.execSQL("delete from equipmentType");
            db.execSQL("VACUUM");
        }
        res.close();
        db.close();
    }

    public void clearDailySalesTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from dailySales");
        db.execSQL("VACUUM");
        db.close();
    }

    public void insertDailySalesEntry(String orderNumber,String itemNo,String serviceType,
                                      String quantity,String total,String buyerType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO dailySales VALUES('"+orderNumber+"','"+itemNo+"','"+serviceType+"','"
                +quantity+"','"+total+"','"+buyerType+"');");
        db.close();
    }

    public String getTotalSaleFromServiceType(String serviceType,String buyerType){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select totalPrice from dailySales where serviceType = '"+serviceType+"'" +
                        "and buyerType ='"+buyerType+"'"
                , null);
        float total = 0;
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String totalPrice = cursor.getString(cursor.getColumnIndex("totalPrice"));
                total += Float.parseFloat(totalPrice);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return String.valueOf(total);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertUserData(Context context){

        SQLiteDatabase db = this.getWritableDatabase();
        for(User user : readUserXML(context)) {
            db.execSQL("INSERT INTO Users VALUES('"+user.getUserName()+"','"+user.getPassword()+"');");
        }
        db.close();
    }

    public void insertFlightData(Context context){

        SQLiteDatabase db = this.getWritableDatabase();
        for(Flight flight : readFlightsXML(context)) {
            db.execSQL("INSERT INTO flights VALUES" +
                    "('"+flight.getFlightName()+"','"+flight.getFlightFrom()+"','"+flight.getFlightTo()+"');");
        }
        db.close();
    }

    public  String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public void insertItemData(Context context){
        try {
            File xml = new File(context.getFilesDir(), "item_list.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("Items");
            JSONArray itemsArr = data.getJSONArray("Item");
            List<Item> list = gson.fromJson(itemsArr.toString(), new TypeToken<List<Item>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(Item item : list){
                db.execSQL("INSERT INTO items VALUES" +
                        "('"+item.getItemNo()+"','"+item.getItemName()+"','"+item.getItemHHC()+"'," +
                        "'"+item.getCategory()+"','"+item.getCatCode()+"','"+item.getCatlogNo()+"','"+item.getPrice()+"'," +
                        "'"+item.getPaxDiscPrice()+"','"+item.getStaffDiscPrice()+"','"+item.getDelist()+"'," +
                        "'"+item.getDfsrOrder()+"','"+item.getServiceType()+"','"+item.getScPrice()+"'," +
                        "'"+item.getBaseCurrency()+"','"+item.getBasePrice()+"','"+item.getSecondCurrency()+"'," +
                        "'"+item.getSecondPrice()+"','"+item.getActiveDate()+"','"+item.getWeight()+"');");
            }
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertKITList(Context context){
        try {
            File xml = new File(context.getFilesDir(), "kit_list.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("KITItems");
            JSONArray itemsArr = data.getJSONArray("KITItem");
            List<KITItem> kitList = gson.fromJson(itemsArr.toString(), new TypeToken<List<KITItem>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(KITItem item : kitList){
                db.execSQL("INSERT INTO KITList VALUES" +
                        "('"+item.getEquipmentNo()+"','"+item.getItemNo()+"','"+item.getItemDescription()+"'," +
                        "'"+item.getQuantity()+"','"+item.getDrawer()+"');");
            }
            db.close();
            insertDrawerValidation();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void insertDrawerValidation(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select equipmentNo,drawer from KITList", null);
        Map<String,List<String>> cartDrawerMap = new HashMap<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String cartNo = cursor.getString(cursor.getColumnIndex("equipmentNo"));
                String drawer = cursor.getString(cursor.getColumnIndex("drawer"));
                if(cartDrawerMap.containsKey(cartNo)){
                    if(!cartDrawerMap.get(cartNo).contains(drawer)){
                        cartDrawerMap.get(cartNo).add(drawer);
                    }
                }
                else {
                    List<String> drawerList = new ArrayList<>();
                    drawerList.add(drawer);
                    cartDrawerMap.put(cartNo,drawerList);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        SQLiteDatabase db1 = this.getWritableDatabase();
        if(!cartDrawerMap.isEmpty()){
            for(Map.Entry<String,List<String>> entry : cartDrawerMap.entrySet()){
                for(String drawer : entry.getValue()) {
                    db1.execSQL("INSERT INTO drawerValidation VALUES" +
                            "('" + entry.getKey() + "','" + drawer + "','NO');");
                }
            }
        }
        db1.close();
    }

    public boolean isDrawerValidated(String cartNo,String drawer){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select isValidated from drawerValidation where equipmentNo = '"+cartNo+"' " +
                        "and drawer = '"+drawer+"'"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String isValidated = cursor.getString(cursor.getColumnIndex("isValidated"));
                cursor.close();
                db.close();
                return "YES".equals(isValidated);
            }
        }
        cursor.close();
        db.close();
        return false;
    }

    public void updateDrawerValidation(String cartNo, String drawer,String validation){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update drawerValidation set isValidated = '"+validation+"' where  equipmentNo = '"+cartNo
                +"' and drawer = '"+drawer+"';");
        db.close();
    }

    public void insertKITNumbersList(Context context){
        try {
            File xml = new File(context.getFilesDir(), "kit_number_list.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("KITNumbers");
            JSONArray itemsArr = data.getJSONArray("KITNumber");
            List<KitNumber> kitNumbers = gson.fromJson(itemsArr.toString(), new TypeToken<List<KitNumber>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(KitNumber item : kitNumbers){
                db.execSQL("INSERT INTO KITNumberList VALUES" +
                        "('"+item.getKitCode()+"','"+item.getKitDesc()+"','"+item.getServiceType()+"'," +
                        "'"+item.getActiveDate()+"','"+item.getNoOfEq()+"','"+item.getNoOfSeals()+"','"+item.getEquipment()+"');");
            }
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertEquipmentTypeList(Context context){
        try {
            File xml = new File(context.getFilesDir(), "equipment_type.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("Equipments");
            JSONArray itemsArr = data.getJSONArray("Equipment");
            List<Equipment> equipmentList = gson.fromJson(itemsArr.toString(), new TypeToken<List<Equipment>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(Equipment item : equipmentList){
                db.execSQL("INSERT INTO equipmentType VALUES" +
                        "('"+item.getEquipmentNo()+"','"+item.getEquipmentDesc()+"','"+item.getEquipmentType()+"'," +
                        "'"+item.getDrawerPrefix()+"','"+item.getNoOfDrawers()+"','"+item.getKitCode()+"');");
            }
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isLoginSuccess(String userName, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery("select * from Users where " +
                    "Username='" + userName + "' and Password='" + password + "'", null);

            int rowCount = res.getCount();
            db.close();
            res.close();
            return rowCount > 0;
        }
        catch (Exception e){
            return false;
        }
    }

    public String[] getFlightNames(){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from flights", null);
            String[] flightNames = new String[cursor.getCount()];
            int i = 0;
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    flightNames[i] = cursor.getString(cursor.getColumnIndex("flightName"));
                    i++;
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return flightNames;
        }
        catch (Exception e){
            return null;
        }
    }

    public Flight getFlightFromFlightName(String flightName){
        Flight flight = new Flight();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from flights where flightName = '"+flightName+"'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    flight.setFlightName(cursor.getString(cursor.getColumnIndex("flightName")));
                    flight.setFlightFrom(cursor.getString(cursor.getColumnIndex("flightFrom")));
                    flight.setFlightTo(cursor.getString(cursor.getColumnIndex("flightTo")));
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return flight;
        }
        catch (Exception e){
            return null;
        }
    }

    public void updateItemQry(String serviceType,String itemId,String soldQty){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> kitCodeList = new ArrayList<>();
        List<KITItem> items = new ArrayList<>();
        Cursor cursor = db.rawQuery("select kitCode from  KITNumberList where serviceType ='"
                +serviceType+"'  and kitCode in (" +
                "select kitCode from equipmentType where equipmentNo in (" +
                " select equipmentNo from KITList where itemNo = '"+itemId+"'))",null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                kitCodeList.add(cursor.getString(cursor.getColumnIndex("kitCode")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        if(kitCodeList.size() == 1){
            Cursor cursor1 = db.rawQuery("select * from KITList where itemNo = '"+itemId+"' and equipmentNo in (" +
                    "select equipmentNo from equipmentType where " +
                    "kitCode = '"+kitCodeList.get(0)+"')",null);

            if (cursor1.moveToFirst()){
                while(!cursor1.isAfterLast()){
                    KITItem kitItem = new KITItem();
                    kitItem.setEquipmentNo(cursor1.getString(cursor1.getColumnIndex("equipmentNo")));
                    kitItem.setItemNo(cursor1.getString(cursor1.getColumnIndex("itemNo")));
                    kitItem.setQuantity(cursor1.getString(cursor1.getColumnIndex("quantity")));
                    items.add(kitItem);
                    cursor1.moveToNext();
                }
            }
            cursor1.close();
        }
        KITItem item = items.get(0);
        int updateQty = Integer.parseInt(item.getQuantity()) - Integer.parseInt(soldQty);
        db.execSQL("update KITList set quantity = '"+String.valueOf(updateQty)+"' where  equipmentNo = '"+item.getEquipmentNo()
                +"' and itemNo = '"+item.getItemNo()+"';");
        db.close();
    }

    public List<String> getItemCatFromItems(String serviceType){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> categoryList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select distinct category from items where itemNo in " +
                    "( select itemNo from  KITList where  equipmentNo in (  " +
                    "select equipmentNo from equipmentType where kitCode in  " +
                    "(select kitCode from KITNumberList where serviceType = '" + serviceType + "')))", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    categoryList.add(cursor.getString(cursor.getColumnIndex("category")));
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return categoryList;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Item> getItemListFromItemCategory(String category){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> itemList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select * from items where category = '" + category + "'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    Item item = new Item();
                    item.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                    item.setItemName(cursor.getString(cursor.getColumnIndex("itemName")));
                    item.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                    itemList.add(item);
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return itemList;
    }

    private String getItemDescFromItemNo(String itemNo){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select itemName from items where itemNo = '" +itemNo+"'",null);
        String itemName = "";
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                itemName = cursor.getString(cursor.getColumnIndex("itemName"));
                cursor.moveToNext();
            }
        }
        return itemName;
    }

    public Map<String,List<KITItem>> getDrawerKitItemMapFromServiceType(String serviceType){
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String,List<KITItem>> drawerKitItemMap = new HashMap<>();
        try {
            Cursor cursor = db.rawQuery("select * from  KITList where  equipmentNo in (  " +
                    "select equipmentNo from equipmentType where kitCode in  " +
                    "(select kitCode from KITNumberList where serviceType = '" + serviceType + "'))", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    KITItem kitItem = new KITItem();
                    String itemNo = cursor.getString(cursor.getColumnIndex("itemNo"));
                    kitItem.setItemNo(itemNo);
                    kitItem.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                    kitItem.setItemDescription(getItemDescFromItemNo(itemNo));
                    String drawer = cursor.getString(cursor.getColumnIndex("drawer"));
                    kitItem.setDrawer(drawer);
                    String equipmentNo = cursor.getString(cursor.getColumnIndex("equipmentNo"));
                    kitItem.setEquipmentNo(equipmentNo);
                    if(drawerKitItemMap.containsKey(drawer)){
                        drawerKitItemMap.get(drawer).add(kitItem);
                    }
                    else{
                        List<KITItem> kitItems = new ArrayList<>();
                        kitItems.add(kitItem);
                        drawerKitItemMap.put(drawer,kitItems);
                    }
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return drawerKitItemMap;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void updateItemCountOfKITItems(KITItem item){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update KITList set quantity = '"+item.getQuantity()+"' where  equipmentNo = '"+item.getEquipmentNo()
        +"' and itemNo = '"+item.getItemNo()+"' and drawer = '"+item.getDrawer()+"';");
        db.close();
    }

    public List<Equipment> getEquipmentList(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Equipment> equipments= new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from equipmentType", null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                Equipment equipment = new Equipment();
                equipment.setEquipmentNo(cursor.getString(cursor.getColumnIndex("equipmentNo")));
                equipment.setEquipmentDesc(cursor.getString(cursor.getColumnIndex("equipmentDesc")));
                equipment.setEquipmentType(cursor.getString(cursor.getColumnIndex("equipmentType")));
                equipment.setDrawerPrefix(cursor.getString(cursor.getColumnIndex("drawerPrefix")));
                equipment.setNoOfDrawers(cursor.getString(cursor.getColumnIndex("noOfDrawers")));
                equipment.setKitCode(cursor.getString(cursor.getColumnIndex("kitCode")));
                equipments.add(equipment);
                cursor.moveToNext();
            }
        }
        db.close();
        cursor.close();
        return equipments;
    }

    private List<String> getEquipmentsFromKITCodes(String serviceType){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> equipmentNumbers = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select * from equipmentType where kitCode in  " +
                    "(select kitCode from KITNumberList where serviceType = '" + serviceType + "')", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    equipmentNumbers.add(cursor.getString(cursor.getColumnIndex("equipmentNo")));
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return equipmentNumbers;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private List<String> getKitCodesFromServiceType(String serviceType){

        SQLiteDatabase db = this.getReadableDatabase();
        List<String> kitCodeList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select kitCode from KITNumberList where serviceType = '" + serviceType + "'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    kitCodeList.add(cursor.getString(cursor.getColumnIndex("kitCode")));
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return kitCodeList;
    }

    private List<Flight> readFlightsXML(Context context){
        List<Flight> itemList = new ArrayList<>();
        try {
            Document doc = getXMLDoc(context,"flights");
            if(doc != null) {
                Element element = doc.getDocumentElement();
                element.normalize();
                NodeList nList = doc.getElementsByTagName("flight");
                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element2 = (Element) node;
                        Flight item = new Flight();
                        item.setFlightName(((Element) node).getAttribute("flightName"));
                        item.setFlightFrom(getValue("from", element2));
                        item.setFlightTo(getValue("to", element2));
                        itemList.add(item);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return itemList;
    }

    private List<User> readUserXML(Context context){
        List<User> itemList = new ArrayList<>();
        try {
            Document doc = getXMLDoc(context,"users");
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

    private Document getXMLDoc(Context context,String fileName) {
        try{
            File f = new File(context.getFilesDir(),fileName + ".xml");
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
