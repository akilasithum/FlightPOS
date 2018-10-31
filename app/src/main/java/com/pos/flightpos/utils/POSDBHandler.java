package com.pos.flightpos.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pos.flightpos.InventoryReportActivity;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.Flight;
import com.pos.flightpos.objects.OrderDetails;
import com.pos.flightpos.objects.SoldItem;
import com.pos.flightpos.objects.User;
import com.pos.flightpos.objects.XMLMapper.ComboDiscount;
import com.pos.flightpos.objects.XMLMapper.Currency;
import com.pos.flightpos.objects.XMLMapper.Equipment;
import com.pos.flightpos.objects.XMLMapper.Item;
import com.pos.flightpos.objects.XMLMapper.Items;
import com.pos.flightpos.objects.XMLMapper.KITItem;
import com.pos.flightpos.objects.XMLMapper.KitNumber;
import com.pos.flightpos.objects.XMLMapper.PreOrder;
import com.pos.flightpos.objects.XMLMapper.Promotion;
import com.pos.flightpos.objects.XMLMapper.Sector;

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
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS flights (flightName VARCHAR,flightFrom VARCHAR," +
                "flightTo VARCHAR,sectors VARCHAR);");
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
                "drawer VARCHAR,isValidated VARCHAR,userMode VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS dailySales (orderNumber VARCHAR,itemNo VARCHAR," +
                "equipmentNo VARCHAR,drawer VARCHAR,quantity VARCHAR,serviceType VARCHAR," +
                "totalPrice VARCHAR,buyerType VARCHAR,sellarName VARCHAR,date VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currency (currencyCode VARCHAR,currencyDesc VARCHAR," +
                "currencyRate VARCHAR, currencyType VARCHAR,priorityOrder VARCHAR,effectiveDate VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS preOrders (PNR VARCHAR,customerName VARCHAR," +
                "serviceType VARCHAR, itemCategory VARCHAR,itemId VARCHAR,quantity VARCHAR,delivered VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sealDetails (sealType VARCHAR,numOfSeals VARCHAR," +
                "seals VARCHAR, sealAddedTime VARCHAR, flightName VARCHAR, flightDate VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS promotions (promotionId VARCHAR,serviceType VARCHAR," +
                "itemId VARCHAR, discount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS comboDiscounts (comboId VARCHAR,discount VARCHAR," +
                "items VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS userComments (userId VARCHAR,area VARCHAR," +
                "comment VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS paymentMethods (orderNumber VARCHAR,paymentType VARCHAR," +
                "amount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS orderMainDetails (orderNumber VARCHAR,tax VARCHAR," +
                "discount VARCHAR, seatNo VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS creditCardDetails (orderNumber VARCHAR,creditCardNumber VARCHAR," +
                "cardHolderName VARCHAR, expireDate VARCHAR , amount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS loyaltyCardDetails (orderNumber VARCHAR,loyaltyCardNumber VARCHAR," +
                "cardHolderName VARCHAR, amount VARCHAR);");
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
        db.execSQL("delete from paymentMethods");
        db.execSQL("delete from creditCardDetails");
        db.execSQL("delete from orderMainDetails");
        db.execSQL("delete from preOrders");
        db.close();
    }

    public void insertUserComments(String userId,String area,String comment){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO userComments VALUES('"+userId+"','"+area+"','"+comment+"');");
        db.close();
    }

    public void clearDailySalesItem(String orderId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from dailySales where orderNumber ='"+orderId+"'");
        db.close();
    }

    public void insertSealData(String sealType,String numOfSeals,String seals,String date,String flightName,String flightDate){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO sealDetails VALUES ('"+sealType+"','"+numOfSeals+"','"+seals+"'," +
                "'"+date+"','"+flightName+"','"+flightDate+"' )");
        db.close();
    }

    public void insertDailySalesEntry(String orderNumber,String itemNo,String serviceType,String equipmentNo,String drawer,
                                      String quantity,String total,String buyerType,String sellerId,String date){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO dailySales VALUES('"+orderNumber+"','"+itemNo+"','"+equipmentNo+"','"+drawer+"','"
                +quantity+"','"+serviceType+"', '"+total+"','"+buyerType+"','"+sellerId+"','"+date+"');");
        db.close();
    }

    public void insertPaymentMethods(String orderNumber,String paymentType,String amount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO paymentMethods VALUES('"+orderNumber+"','"+paymentType+"','"+amount+"');");
        db.close();
    }

    public Map<String,String> getPaymentMethodsMapFromOrderNumber(String orderId){
        SQLiteDatabase db = this.getWritableDatabase();
        Map<String,String> items = new HashMap<>();
        Cursor cursor = db.rawQuery("select * from paymentMethods where orderNumber = '"+orderId+"'"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String type = cursor.getString(cursor.getColumnIndex("paymentType"));
                String amount = cursor.getString(cursor.getColumnIndex("amount"));
                items.put(type,amount);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return items;
    }

    public void insertOrderMainDetails(String orderNumber,String tax,String discount,String seatNo){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO orderMainDetails VALUES('"+orderNumber+"','"+tax+"','"+discount+"','"+seatNo+"');");
        db.close();
    }

    public OrderDetails getOrderDetailsFromOrderNumber(String orderId){
        SQLiteDatabase db = this.getWritableDatabase();
        OrderDetails details = new OrderDetails();
        Cursor cursor = db.rawQuery("select * from orderMainDetails where orderNumber = '"+orderId+"'"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                details.setOrderNumber(cursor.getString(cursor.getColumnIndex("orderNumber")));
                details.setTax(cursor.getString(cursor.getColumnIndex("tax")));
                details.setDiscount(cursor.getString(cursor.getColumnIndex("discount")));
                details.setSeatNo(cursor.getString(cursor.getColumnIndex("seatNo")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return details;
    }

    public void insertCreditCardDetails(String orderNumber,String creditCardNumber,String cardHolderName,
                                        String expireDate,String amount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO creditCardDetails VALUES('"+orderNumber+"','"+creditCardNumber+"'," +
                "'"+cardHolderName+"','"+expireDate+"','"+amount+"');");
        db.close();
    }
    public void insertLoyaltyCardDetails(String orderNumber,String loyaltyCardNumber,String cardHolderName,
                                        String amount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO loyaltyCardDetails VALUES('"+orderNumber+"','"+loyaltyCardNumber+"'," +
                "'"+cardHolderName+"','"+amount+"');");
        db.close();
    }

    public CreditCard getCreditCardDetailsFromOrderNumber(String orderId){
        SQLiteDatabase db = this.getWritableDatabase();
        CreditCard details = new CreditCard();
        Cursor cursor = db.rawQuery("select * from creditCardDetails where orderNumber = '"+orderId+"'"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                details.setCreditCardNumber(cursor.getString(cursor.getColumnIndex("creditCardNumber")));
                details.setCardHolderName(cursor.getString(cursor.getColumnIndex("cardHolderName")));
                details.setExpireDate(cursor.getString(cursor.getColumnIndex("expireDate")));
                details.setPaidAmount(Float.parseFloat(cursor.getString
                        (cursor.getColumnIndex("amount"))));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return details;
    }

    public List<SoldItem> getSoldItemsFromOrderId(String orderId){
        SQLiteDatabase db = this.getWritableDatabase();
        List<SoldItem> items = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from dailySales where orderNumber = '"+orderId+"'"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                SoldItem item = new SoldItem();

                item.setItemDesc(getItemDescFromItemNo(cursor.getString(cursor.getColumnIndex("itemNo"))));
                item.setItemId(cursor.getString(cursor.getColumnIndex("itemNo")));
                item.setPrice(cursor.getString(cursor.getColumnIndex("totalPrice")));
                item.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                item.setEquipmentNo(cursor.getString(cursor.getColumnIndex("equipmentNo")));
                item.setDrawer(cursor.getString(cursor.getColumnIndex("drawer")));
                items.add(item);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return items;
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
            String sectors = "";
            if(flight.getSectorList() != null && !flight.getSectorList().isEmpty()){
                for(Sector sector : flight.getSectorList()){
                    sectors += sector.getFrom() +"+"+sector.getTo() + ",";
                }
                sectors.substring(0,sectors.length()-2);
            }
            db.execSQL("INSERT INTO flights VALUES" +
                    "('"+flight.getFlightName()+"','"+flight.getFlightFrom()+"','"+flight.getFlightTo()+"','"+sectors+"');");
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
                            "('" + entry.getKey() + "','" + drawer + "','NO','');");
                }
            }
        }
        db1.close();
    }

    public boolean isDrawerValidated(String cartNo,String drawer,String userMode){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select isValidated from drawerValidation where equipmentNo = '"+cartNo+"' " +
                        "and drawer = '"+drawer+"' and userMode = '"+userMode+"'"
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

    public void updateDrawerValidation(String cartNo, String drawer,String validation,String userMode){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update drawerValidation set isValidated = '"+validation+"',userMode = '"+userMode+"' " +
                "where  equipmentNo = '"+cartNo
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

    public boolean insertEquipmentTypeList(Context context){
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
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertCurrencyData(Context context){

        try {
            File xml = new File(context.getFilesDir(), "currency.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("currencies");
            JSONArray itemsArr = data.getJSONArray("currency");
            List<Currency> currencyList = gson.fromJson(itemsArr.toString(), new TypeToken<List<Currency>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(Currency currency : currencyList){
                db.execSQL("INSERT INTO currency VALUES" +
                        "('"+currency.getCurrencyCode()+"','"+currency.getCurrencyDesc()+"','"+currency.getCurrencyRate()+"'," +
                        "'"+currency.getCurrencyType()+"','"+currency.getPriorityOrder()+"','"+currency.getEffectiveDate()+"');");
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertPreOrders(Context context){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            File xml = new File(context.getFilesDir(), "pre_orders.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("preOrders");
            JSONArray itemsArr = data.getJSONArray("preOrder");
            List<PreOrder> preOrders = gson.fromJson(itemsArr.toString(), new TypeToken<List<PreOrder>>(){}.getType());
            for(PreOrder preOrder : preOrders){
                db.execSQL("INSERT INTO preOrders VALUES" +
                        "('"+preOrder.getPNR()+"','"+preOrder.getCustomerName()+"','"+preOrder.getServiceType()+"'," +
                        "'"+preOrder.getItemCategory()+"','"+preOrder.getItemId()+"','"+preOrder.getQuantity()+"','Not Delivered');");
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertPromotions(Context context){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            File xml = new File(context.getFilesDir(), "promotions.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("promotions");
            JSONArray itemsArr = data.getJSONArray("promotion");
            List<Promotion> promotions = gson.fromJson(itemsArr.toString(), new TypeToken<List<Promotion>>(){}.getType());
            for(Promotion promotion : promotions){
                db.execSQL("INSERT INTO promotions VALUES" +
                        "('"+promotion.getPromotionId()+"','"+promotion.getServiceType()+"','"+promotion.getItemId()+"'," +
                        "'"+promotion.getDiscount()+"');");
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertComboDiscount(Context context){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            File xml = new File(context.getFilesDir(), "combo_discount.xml");
            JSONObject jsonObj  = XML.toJSONObject(readStream(new FileInputStream(xml)));
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("comboDiscounts");
            JSONArray itemsArr = data.getJSONArray("comboDiscount");
            List<ComboDiscount> comboDiscounts = gson.fromJson(itemsArr.toString(), new TypeToken<List<ComboDiscount>>(){}.getType());
            for(ComboDiscount comboDiscount : comboDiscounts){
                db.execSQL("INSERT INTO comboDiscounts VALUES" +
                        "('"+comboDiscount.getComboId()+"','"+comboDiscount.getDiscount()+"','"+comboDiscount.getItems()+"');");
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Promotion> getPromotionsFromServiceType(String serviceType){
        List<Promotion> promotions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from promotions where serviceType = '"+serviceType+"'", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                Promotion promotion = new Promotion();
                promotion.setPromotionId(cursor.getString(cursor.getColumnIndex("promotionId")));
                promotion.setServiceType((cursor.getString(cursor.getColumnIndex("serviceType"))));
                promotion.setItemId((cursor.getString(cursor.getColumnIndex("itemId"))));
                promotion.setDiscount((cursor.getString(cursor.getColumnIndex("discount"))));
                promotions.add(promotion);
                cursor.moveToNext();
            }
        }
        return promotions;
    }

    public List<ComboDiscount> getComboDiscounts(){
        List<ComboDiscount> comboDiscounts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from comboDiscounts", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                ComboDiscount comboDiscount = new ComboDiscount();
                comboDiscount.setComboId((cursor.getString(cursor.getColumnIndex("comboId"))));
                comboDiscount.setItems((cursor.getString(cursor.getColumnIndex("items"))));
                comboDiscount.setDiscount((cursor.getString(cursor.getColumnIndex("discount"))));
                comboDiscounts.add(comboDiscount);
                cursor.moveToNext();
            }
        }
        return comboDiscounts;
    }

    public List<Currency> getCurrencyList(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Currency> currencyList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select * from currency", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    Currency currency = new Currency();
                    currency.setCurrencyCode(cursor.getString(cursor.getColumnIndex("currencyCode")));
                    currency.setCurrencyDesc(cursor.getString(cursor.getColumnIndex("currencyDesc")));
                    currency.setCurrencyRate(cursor.getString(cursor.getColumnIndex("currencyRate")));
                    currency.setCurrencyType(cursor.getString(cursor.getColumnIndex("currencyType")));
                    currency.setPriorityOrder(cursor.getString(cursor.getColumnIndex("priorityOrder")));
                    currency.setEffectiveDate(cursor.getString(cursor.getColumnIndex("effectiveDate")));
                    currencyList.add(currency);
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return currencyList;
        }
        catch (Exception e){
            return null;
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
            Cursor cursor = db.rawQuery("select * from flights where flightName like '%"+flightName+"'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    flight.setFlightName(cursor.getString(cursor.getColumnIndex("flightName")));
                    flight.setFlightFrom(cursor.getString(cursor.getColumnIndex("flightFrom")));
                    flight.setFlightTo(cursor.getString(cursor.getColumnIndex("flightTo")));
                    flight.setSectorStr(cursor.getString(cursor.getColumnIndex("sectors")));
                    cursor.moveToNext();
                }
            }
            else{
                return null;
            }
            db.close();
            cursor.close();
            return flight;
        }
        catch (Exception e){
            return null;
        }
    }

    public void updateSoldItemQty(String itemNo,String soldQty,String equipmentNo,String drawer){
        SQLiteDatabase db = this.getReadableDatabase();
        int currentQty = 0;
        Cursor cursor = db.rawQuery("select quantity from KITList where equipmentNo = '"+equipmentNo +
                "' and itemNo = '"+itemNo+"' and drawer = '"+drawer+"'",null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                currentQty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("quantity")));
                cursor.moveToNext();
            }
        }
        int updateQty = currentQty - Integer.parseInt(soldQty);
        db.execSQL("update KITList set quantity = '"+String.valueOf(updateQty)+"' where  equipmentNo = '"+equipmentNo
                +"' and itemNo = '"+itemNo+"' and drawer = '"+drawer+"';");
    }

    public List<String> getItemCatFromItems(String kitCode){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> categoryList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select distinct category from items where itemNo in " +
                    "( select itemNo from  KITList where  equipmentNo in (  " +
                    "select equipmentNo from equipmentType where kitCode = '"+kitCode+"'  ))", null);
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

    public Map<String,List<PreOrder>> getAvailablePreOrders(String selectedServiceType){

        Map<String,List<PreOrder>> serviceTypePreOrderMap = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from preOrders", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    PreOrder preOrder = new PreOrder();
                    preOrder.setPNR(cursor.getString(cursor.getColumnIndex("PNR")));
                    preOrder.setCustomerName(cursor.getString(cursor.getColumnIndex("customerName")));
                    preOrder.setItemCategory(cursor.getString(cursor.getColumnIndex("itemCategory")));
                    preOrder.setItemId(cursor.getString(cursor.getColumnIndex("itemId")));
                    preOrder.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                    String serviceType = cursor.getString(cursor.getColumnIndex("serviceType"));
                    preOrder.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                    preOrder.setDelivered(cursor.getString(cursor.getColumnIndex("delivered")));

                    if(serviceTypePreOrderMap.containsKey(serviceType)){
                        serviceTypePreOrderMap.get(serviceType).add(preOrder);
                    }
                    else{
                        List<PreOrder> preOrders = new ArrayList<>();
                        preOrders.add(preOrder);
                        serviceTypePreOrderMap.put(serviceType,preOrders);
                    }

                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return serviceTypePreOrderMap;
    }

    public void updatePreOrderDeliveryStatus(String deliveryStatus,String PNR,String itemId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update preOrders set delivered = '"+deliveryStatus+"' where  PNR = '"+PNR+"' and " +
                "itemId = '"+itemId+"';");
        db.close();

    }

    public List<SoldItem> getItemListFromItemCategory(String category,String kitCode){
        SQLiteDatabase db = this.getReadableDatabase();
        List<SoldItem> itemList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("SELECT items.itemNo as itemNo,items.itemName as itemName," +
                    "items.price as price,KITList.equipmentNo as equipmentNo," +
                    "KITList.drawer as drawer FROM (SELECT * FROM items where category = '"+category+"') as items INNER JOIN " +
                    "(SELECT * FROM KITList WHERE equipmentNo in (select equipmentNo from equipmentType " +
                    "where kitCode = '"+kitCode+"')) as KITList ON items.itemNo = KITList.itemNo", null);

            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    SoldItem item = new SoldItem();
                    item.setItemId(cursor.getString(cursor.getColumnIndex("itemNo")));
                    item.setItemDesc(cursor.getString(cursor.getColumnIndex("itemName")));
                    item.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                    item.setEquipmentNo(cursor.getString(cursor.getColumnIndex("equipmentNo")));
                    item.setDrawer(cursor.getString(cursor.getColumnIndex("drawer")));
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

    public String getItemDescFromItemNo(String itemNo){
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

    public String getKitNumberListFieldValueFromKitCode(String kitCode,String fieldVal){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select "+fieldVal+" from KITNumberList where kitCode = '" +kitCode+"'",null);
        String serviceType = "";
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                serviceType = cursor.getString(cursor.getColumnIndex(fieldVal));
                cursor.moveToNext();
            }
        }
        return serviceType;
    }

    public Map<String,Map<String,List<KITItem>>> getDrawerKitItemMapFromServiceType(String kitCode){
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String,Map<String,List<KITItem>>> drawerKitItemMap = new HashMap<>();
        try {
            Cursor cursor = db.rawQuery("select * from  KITList where  equipmentNo in (  " +
                    "select equipmentNo from equipmentType where kitCode =  '"+kitCode+"')", null);
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
                    if(drawerKitItemMap.containsKey(equipmentNo)){
                        if(drawerKitItemMap.get(equipmentNo).containsKey(drawer)){
                            drawerKitItemMap.get(equipmentNo).get(drawer).add(kitItem);
                        }
                        else{
                            List<KITItem> kitItems = new ArrayList<>();
                            kitItems.add(kitItem);
                            drawerKitItemMap.get(equipmentNo).put(drawer,kitItems);
                        }
                    }
                    else {
                        Map<String,List<KITItem>> listMap = new HashMap<>();
                        List<KITItem> kitItems = new ArrayList<>();
                        kitItems.add(kitItem);
                        listMap.put(drawer,kitItems);
                        drawerKitItemMap.put(equipmentNo,listMap);
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

    public List<KitNumber> getKITCodeList(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<KitNumber> kitCodes= new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from KITNumberList", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                KitNumber kitNumber = new KitNumber();
                kitNumber.setKitCode(cursor.getString(cursor.getColumnIndex("kitCode")));
                kitNumber.setKitDesc(cursor.getString(cursor.getColumnIndex("kitDesc")));
                kitNumber.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                kitNumber.setNoOfEq(cursor.getString(cursor.getColumnIndex("noOfEq")));
                kitNumber.setNoOfSeals(cursor.getString(cursor.getColumnIndex("noOfSeals")));
                kitCodes.add(kitNumber);
                cursor.moveToNext();
            }
        }
        db.close();
        cursor.close();
        return kitCodes;
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
        List<Flight> flights = new ArrayList<>();
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
                        Flight flight = new Flight();
                        flight.setFlightName(((Element) node).getAttribute("flightName"));
                        flight.setFlightFrom(getValue("from", element2));
                        flight.setFlightTo(getValue("to", element2));
                        flight.setSectorList(getSectors(element2));
                        flights.add(flight);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flights;
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

    private static List<Sector> getSectors(Element element) {
        List<Sector> sectors = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName("sector");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element2 = (Element) node;
                Sector flight = new Sector();
                flight.setFrom(getValue("from", element2));
                flight.setTo(getValue("to", element2));
                sectors.add(flight);
            }
        }
        return sectors;
    }
}
