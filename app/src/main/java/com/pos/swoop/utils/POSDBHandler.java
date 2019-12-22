package com.pos.swoop.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pos.swoop.objects.AcceptPreOrder;
import com.pos.swoop.objects.AcceptPreOrderItem;
import com.pos.swoop.objects.Constants;
import com.pos.swoop.objects.CreditCard;
import com.pos.swoop.objects.Flight;
import com.pos.swoop.objects.FullFlight;
import com.pos.swoop.objects.OrderDetails;
import com.pos.swoop.objects.Sector;
import com.pos.swoop.objects.SoldItem;
import com.pos.swoop.objects.User;
import com.pos.swoop.objects.XMLMapper.BondMessage;
import com.pos.swoop.objects.XMLMapper.CartNumber;
import com.pos.swoop.objects.XMLMapper.ComboDiscount;
import com.pos.swoop.objects.XMLMapper.Currency;
import com.pos.swoop.objects.XMLMapper.Equipment;
import com.pos.swoop.objects.XMLMapper.FADetails;
import com.pos.swoop.objects.XMLMapper.FAMessage;
import com.pos.swoop.objects.XMLMapper.Item;
import com.pos.swoop.objects.XMLMapper.ItemSale;
import com.pos.swoop.objects.XMLMapper.KITItem;
import com.pos.swoop.objects.XMLMapper.KitNumber;
import com.pos.swoop.objects.XMLMapper.POSFlight;
import com.pos.swoop.objects.XMLMapper.PaymentMethods;
import com.pos.swoop.objects.XMLMapper.PreOrder;
import com.pos.swoop.objects.XMLMapper.PreOrderItem;
import com.pos.swoop.objects.XMLMapper.PreOrderMain;
import com.pos.swoop.objects.XMLMapper.Promotion;
import com.pos.swoop.objects.XMLMapper.SIFDetails;
import com.pos.swoop.objects.XMLMapper.SIFSheet;
import com.pos.swoop.objects.XMLMapper.UserComment;
import com.pos.swoop.objects.XMLMapper.Voucher;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.Arrays;
import java.util.Date;
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
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS flights (flightId VARCHAR,obFlightName VARCHAR,obFlightFrom VARCHAR," +
                "obFlightTo VARCHAR,ibFlightName,ibFlightFrom,ibFlightTo VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS items (itemNo VARCHAR,itemName VARCHAR,itemHHC VARCHAR,category VARCHAR," +
                "catCode VARCHAR,catlogNo VARCHAR,price VARCHAR," +
                "paxDiscPrice VARCHAR,staffDiscPrice VARCHAR,delist VARCHAR,dfsrOrder VARCHAR,serviceType VARCHAR,scPrice VARCHAR," +
                "baseCurrency VARCHAR,basePrice VARCHAR,secondCurrency VARCHAR,secondPrice VARCHAR,activeDate VARCHAR,weight VARCHAR,nfcTag VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS KITList (equipmentNo VARCHAR,itemNo VARCHAR," +
                "itemDescription VARCHAR,quantity VARCHAR,drawer VARCHAR,originalQty VARCHER);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS KITNumberList (kitCode VARCHAR,kitDesc VARCHAR," +
                "serviceType VARCHAR,activeDate VARCHAR,noOfEq VARCHAR,equipment VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS equipmentType (equipmentNo VARCHAR,equipmentDesc VARCHAR," +
                "equipmentType VARCHAR,drawerPrefix VARCHAR,noOfDrawers VARCHAR,noOfSeals VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS drawerValidation (equipmentNo VARCHAR," +
                "drawer VARCHAR,isValidated VARCHAR,userMode VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS dailySales (orderNumber VARCHAR,itemNo VARCHAR," +
                "equipmentNo VARCHAR,drawer VARCHAR,quantity VARCHAR,serviceType VARCHAR," +
                "totalPrice VARCHAR,buyerType VARCHAR,sellarName VARCHAR,date VARCHAR,flightType VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currency (currencyCode VARCHAR,currencyDesc VARCHAR," +
                "currencyRate VARCHAR, currencyType VARCHAR,priorityOrder VARCHAR,effectiveDate VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS preOrders (preOrderId VARCHAR,PNR VARCHAR,customerName VARCHAR," +
                "serviceType VARCHAR,delivered VARCHAR,adminStatus VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sealDetails (sealType VARCHAR,serviceType VARCHAR,numOfSeals VARCHAR," +
                "sealNo VARCHAR, sealAddedTime VARCHAR, flightName VARCHAR, flightDate VARCHAR,isVerified VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS promotions (promotionId VARCHAR,serviceType VARCHAR," +
                "itemId VARCHAR, discount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS comboDiscounts (comboId VARCHAR,discount VARCHAR," +
                "items VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS userComments (userId VARCHAR,flightNo VARCHAR,flightDate VARCHAR, area VARCHAR," +
                "comment VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS paymentMethods (orderNumber VARCHAR,paymentType VARCHAR," +
                "amount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS orderMainDetails (orderNumber VARCHAR,tax VARCHAR," +
                "discount VARCHAR, seatNo VARCHAR,subTotal VARCHAR,serviceType VARCHAR,flightId VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS creditCardDetails (orderNumber VARCHAR,creditCardNumber VARCHAR," +
                "cardHolderName VARCHAR, expireDate VARCHAR , amount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS loyaltyCardDetails (orderNumber VARCHAR,loyaltyCardNumber VARCHAR," +
                "cardHolderName VARCHAR, amount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS acceptPreOrder (orderNumber VARCHAR,paxName VARCHAR," +
                "flightNumber VARCHAR, flightDate VARCHAR,flightSector VARCHAR,pnr VARCHAR,serviceType VARCHAR,amount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS acceptPreOrderItems (orderNumber VARCHAR,itemNo VARCHAR," +
                "itemCategory VARCHAR, quantity VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS preOrderItems (preOrderId VARCHAR,itemNo VARCHAR,category VARCHAR," +
                "quantity VARCHAR,delivered VARCHAR,adminStatus VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS vouchers (voucherId VARCHAR,voucherName VARCHAR,voucherType VARCHAR," +
                "discount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS seals (serviceType VARCHAR,sealType VARCHAR,sealNo VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS posFlights (flightId VARCHAR,flightName VARCHAR,flightDate VARCHAR," +
                "flightFrom VARCHAR,flightTo VARCHAR,paxCount VARCHAR,businessClassPaxCount VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS SIFDetails (sifNo VARCHAR,deviceId VARCHAR,packedFor VARCHAR," +
                "packedDateTime VARCHAR,crewOpenedDateTime VARCHAR,crewClosedDateTime VARCHAR, programs VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS cartNumbers (equipmentType VARCHAR,cartNumber VARCHAR," +
                "serviceType VARCHAR,packType VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sectors (flightNo VARCHAR,sectorFrom VARCHAR" +
                ",sectorTo VARCHAR,sectorType VARCHAR,flightType VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS faDetails (flightNo VARCHAR,sector VARCHAR" +
                ",flightDate VARCHAR,faName VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bondMessages (messageId VARCHAR,messageBody VARCHAR);");
         sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS SIFSheetDetails (itemNo VARCHAR,itemDesc VARCHAR,price VARCHAR,cart VARCHAR,drawer VARCHAR,obOpenQty VARCHAR," +
                "obSoldqty VARCHAR,obClosingQty VARCHAR,ibOpenQty VARCHAR,ibSoldQty VARCHAR,ibClosingQty VARCHAR,serviceType VARCHAR);");
    }

    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from Users", null);
        if(res.getCount() > 0) {
            db.execSQL("delete from Users");
            db.execSQL("delete from flights");
            db.execSQL("delete from sectors");
            db.execSQL("delete from KITList");
            db.execSQL("delete from KITNumberList");
            db.execSQL("delete from items");
            db.execSQL("delete from equipmentType");
            db.execSQL("delete from currency");
            db.execSQL("delete from promotions");
            db.execSQL("delete from comboDiscounts");
            db.execSQL("delete from vouchers");
            db.execSQL("delete from SIFDetails");
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
        db.execSQL("delete from preOrderItems");
        db.execSQL("delete from sealDetails");
        db.execSQL("delete from posFlights");
        db.execSQL("delete from cartNumbers");
        db.execSQL("delete from faDetails");
        db.execSQL("delete from SIFSheetDetails");
        db.execSQL("delete from userComments");
        db.execSQL("VACUUM");
        db.close();
        resetDrawerValidation();
    }

    public List<SIFSheet> getSifSheetDetails(){
        List<SIFSheet> sifSheetList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from SIFSheetDetails", null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                SIFSheet sheet = new SIFSheet();
                sheet.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                sheet.setItemDesc(cursor.getString(cursor.getColumnIndex("itemDesc")));
                sheet.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                sheet.setCart(cursor.getString(cursor.getColumnIndex("cart")));
                sheet.setDrawer(cursor.getString(cursor.getColumnIndex("drawer")));
                sheet.setObOpenQty(cursor.getString(cursor.getColumnIndex("obOpenQty")));
                sheet.setObSoldQty(cursor.getString(cursor.getColumnIndex("obSoldqty")));
                sheet.setObClosingQty(cursor.getString(cursor.getColumnIndex("obClosingQty")));
                sheet.setIbOpenQty(cursor.getString(cursor.getColumnIndex("ibOpenQty")));
                sheet.setIbSoldQty(cursor.getString(cursor.getColumnIndex("ibSoldQty")));
                sheet.setIbClosingQty(cursor.getString(cursor.getColumnIndex("ibClosingQty")));
                sheet.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                sifSheetList.add(sheet);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return sifSheetList;
    }

    public void insertOpeningInventory(List<KITItem> kitItems){

        List<Item> items = getAllItems();
        Map<String,Item> itemNoPriceMap = new HashMap<>();
        for(Item item : items){
            itemNoPriceMap.put(item.getItemNo(),item);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        for(KITItem item : kitItems){
            Item beItem = itemNoPriceMap.get(item.getItemNo());
            db.execSQL("INSERT INTO SIFSheetDetails (itemNo,itemDesc,price,cart,drawer,obOpenQty,serviceType) VALUES('"+item.getItemNo()+"'," +
                    "'"+beItem.getItemName()+"','"+beItem.getPrice()+"','"+item.getEquipmentNo()+"','"+item.getDrawer()+"'," +
                    "'"+item.getQuantity()+"','"+beItem.getServiceType()+"');");
        }
        db.close();
    }

    public void updateOBIBSales(List<KITItem> kitItems,String flightType){

        List<ItemSale> itemSales = getItemSale(flightType);
        Map<String,String> itemIdQtyMap = new HashMap<>();
        for(ItemSale item : itemSales){
            String uniqueKey = item.getItemId()+"-"+item.getEquipmentType()+"-"+item.getDrawer();
            if(itemIdQtyMap.containsKey(uniqueKey)){
                String qty = String.valueOf(Integer.parseInt(itemIdQtyMap.get(uniqueKey))+Integer.parseInt(item.getQuantity()));
                itemIdQtyMap.put(uniqueKey,qty);
            }
            else {
                itemIdQtyMap.put(uniqueKey,item.getQuantity());
            }
        }
        SQLiteDatabase db = this.getReadableDatabase();
        if(flightType.equalsIgnoreCase("outBound")){
            for(KITItem item : kitItems){
                String uniqueKey = item.getItemNo()+"-"+item.getEquipmentNo()+"-"+item.getDrawer();
                if(itemIdQtyMap.containsKey(uniqueKey)){
                    db.execSQL("update SIFSheetDetails set obSoldqty =" +"'"+itemIdQtyMap.get(uniqueKey)+"',obClosingQty='"+item.getQuantity()+"'" +
                            "where itemNo = '"+item.getItemNo()+"' and cart = '"+item.getEquipmentNo()+"' and drawer = '"+item.getDrawer()+"';");
                }
                else{
                    db.execSQL("update SIFSheetDetails set obClosingQty='"+item.getQuantity()+"',obSoldqty='"+0+"'" +
                            "where itemNo = '"+item.getItemNo()+"' and cart = '"+item.getEquipmentNo()+"' and drawer = '"+item.getDrawer()+"';");
                }
            }
        }
        else {
            for(KITItem item : kitItems){
                String uniqueKey = item.getItemNo()+"-"+item.getEquipmentNo()+"-"+item.getDrawer();
                if(itemIdQtyMap.containsKey(uniqueKey)){
                    db.execSQL("update SIFSheetDetails set ibSoldQty =" +"'"+itemIdQtyMap.get(uniqueKey)+"',ibClosingQty='"+item.getQuantity()+"'" +
                            "where itemNo = '"+item.getItemNo()+"' and cart = '"+item.getEquipmentNo()+"' and drawer = '"+item.getDrawer()+"';");
                }
                else{
                    db.execSQL("update SIFSheetDetails set ibClosingQty='"+item.getQuantity()+"',ibSoldQty='"+0+"'" +
                            "where itemNo = '"+item.getItemNo()+"' and cart = '"+item.getEquipmentNo()+"' and drawer = '"+item.getDrawer()+"';");
                }
            }
        }
        db.close();
    }

    public void updateSectorInventory(List<KITItem> kitItems){

        SQLiteDatabase db = this.getReadableDatabase();
        for(KITItem item : kitItems){

                db.execSQL("update SIFSheetDetails set ibOpenQty='"+item.getQuantity()+"'" +
                        "where itemNo = '"+item.getItemNo()+"' and cart = '"+item.getEquipmentNo()+"' and drawer = '"+item.getDrawer()+"';");

        }
        db.close();
    }

    public boolean isSifSheetDetailsAvailable(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from SIFSheetDetails", null);
        if(res.getCount() > 0) {
            res.close();
            db.close();
            return true;
        }
        res.close();
        db.close();
        return false;
    }

    public void insertSIFDetails(String sifNo,String deviceId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO SIFDetails (sifNo,deviceId) VALUES('"+sifNo+"','"+deviceId+"');");
        db.close();
    }

    public void insertFADetails(String flightNo,String sector,String flightDate, List<String> users){
        SQLiteDatabase db = this.getWritableDatabase();
        for(String user : users) {
            db.execSQL("INSERT INTO faDetails VALUES('" + flightNo + "','" + sector + "','"+flightDate+"','"+user+"');");
        }
        db.close();
    }

    public void insertCartNumbers(String equipmentType,String cartNo,String serviceType,String packType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO cartNumbers VALUES('"+equipmentType+"','"+cartNo+"','"+serviceType+"','"+packType+"');");
        db.close();
    }

    public Map<String,String> getEqNoCartNoMap(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from cartNumbers", null);
        Map<String,String> map = new HashMap<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String barcodeVal = cursor.getString(cursor.getColumnIndex("cartNumber"));
                String equipentNo = cursor.getString(cursor.getColumnIndex("equipmentType"));
                map.put(equipentNo,barcodeVal);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return map;
    }

    public void updateCartNumber(String equipmentType,String cartNo){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update cartNumbers set cartNumber =" +"'"+cartNo+"'" +
                "where equipmentType = '"+equipmentType+"';");
        db.close();
    }

    public boolean isCartNumberEntered(String barcode){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from cartNumbers where cartNumber = '"+barcode+"'", null);
        String barcodeVal = "";
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                barcodeVal = cursor.getString(cursor.getColumnIndex("cartNumber"));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return barcodeVal.isEmpty();
    }

    public String getBarcodeFromEquipmentType(String equipmentType){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from cartNumbers where equipmentType = '"+equipmentType+"'", null);
        String barcode = "";
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                barcode = cursor.getString(cursor.getColumnIndex("cartNumber"));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return barcode;
    }

    public List<CartNumber> getCartNumbers(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from cartNumbers", null);
        List<CartNumber> barcode = new ArrayList<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                CartNumber cartNumber = new CartNumber();
                cartNumber.setCartNumber(cursor.getString(cursor.getColumnIndex("cartNumber")));
                cartNumber.setEquipmentType(cursor.getString(cursor.getColumnIndex("equipmentType")));
                cartNumber.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                barcode.add(cartNumber);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return barcode;
    }

    public void updateSIFDetails(String fieldName,String value,String deviceId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update SIFDetails set "+fieldName+" = '"+value+"'" +
                "where deviceId = '"+deviceId+"';");
        db.close();
    }

    public void updateSIFDetailsFromConfigureFlight(String packedFor,String programs,String deviceId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update SIFDetails set packedFor = '"+packedFor+"' , programs = '"+programs+"'" +
                "where deviceId = '"+deviceId+"';");
        db.close();
    }

    public SIFDetails getSIFDetails(String sifNo){
        SIFDetails sif = new SIFDetails();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from SIFDetails where sifNo = '"+sifNo+"'", null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                sif.setSifNo(cursor.getString(cursor.getColumnIndex("sifNo")));
                sif.setDeviceId(cursor.getString(cursor.getColumnIndex("deviceId")));
                sif.setPrograms(cursor.getString(cursor.getColumnIndex("programs")));
                sif.setPackedFor(cursor.getString(cursor.getColumnIndex("packedFor")));
                sif.setPackedTime(cursor.getString(cursor.getColumnIndex("packedDateTime")));
                sif.setCrewOpenedTime(cursor.getString(cursor.getColumnIndex("crewOpenedDateTime")));
                sif.setCrewClosedTime(cursor.getString(cursor.getColumnIndex("crewClosedDateTime")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return sif;
    }

    public SIFDetails getSIFDetails(){
        SIFDetails sif = new SIFDetails();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from SIFDetails", null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                sif.setSifNo(cursor.getString(cursor.getColumnIndex("sifNo")));
                sif.setDeviceId(cursor.getString(cursor.getColumnIndex("deviceId")));
                sif.setPrograms(cursor.getString(cursor.getColumnIndex("programs")));
                sif.setPackedFor(cursor.getString(cursor.getColumnIndex("packedFor")));
                sif.setPackedTime(cursor.getString(cursor.getColumnIndex("packedDateTime")));
                sif.setCrewOpenedTime(cursor.getString(cursor.getColumnIndex("crewOpenedDateTime")));
                sif.setCrewClosedTime(cursor.getString(cursor.getColumnIndex("crewClosedDateTime")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return sif;
    }

    public List<AcceptPreOrder> getAllAcceptPreOrders(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from acceptPreOrder", null);
        List<AcceptPreOrder> acceptPreOrders = new ArrayList<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                AcceptPreOrder preOrder = new AcceptPreOrder();
                preOrder.setOrderNumber(cursor.getString(cursor.getColumnIndex("orderNumber")));
                preOrder.setPaxName(cursor.getString(cursor.getColumnIndex("paxName")));
                preOrder.setFlightNumber(cursor.getString(cursor.getColumnIndex("flightNumber")));
                preOrder.setFlightDate(cursor.getString(cursor.getColumnIndex("flightDate")));
                preOrder.setFlightSector(cursor.getString(cursor.getColumnIndex("flightSector")));
                preOrder.setPnr(cursor.getString(cursor.getColumnIndex("pnr")));
                preOrder.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                preOrder.setAmount(cursor.getString(cursor.getColumnIndex("amount")));
                acceptPreOrders.add(preOrder);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return acceptPreOrders;
    }

    public Map<String,List<AcceptPreOrderItem>> getAllPreOrderItems(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from acceptPreOrderItems", null);
        Map<String,List<AcceptPreOrderItem>> acceptPreOrderMap = new HashMap<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                AcceptPreOrderItem preOrder = new AcceptPreOrderItem();
                String preOrderId = cursor.getString(cursor.getColumnIndex("orderNumber"));
                preOrder.setOrderNumber(preOrderId);
                preOrder.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                preOrder.setItemCategory(cursor.getString(cursor.getColumnIndex("itemCategory")));
                preOrder.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));

                if(acceptPreOrderMap.containsKey(preOrderId)){
                    acceptPreOrderMap.get(preOrderId).add(preOrder);
                }
                else{
                    List<AcceptPreOrderItem> preOrders = new ArrayList<>();
                    preOrders.add(preOrder);
                    acceptPreOrderMap.put(preOrderId,preOrders);
                }

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return acceptPreOrderMap;
    }

    public void insertUserComments(String userId,String flightNo,String area,String comment){
        SQLiteDatabase db = this.getWritableDatabase();
        String flightDate = POSCommonUtils.getDateString(new Date());
        db.execSQL("INSERT INTO userComments VALUES('"+userId+"','"+flightNo+"','"+flightDate+"','"+area+"','"+comment+"');");
        db.close();
    }

    public void insertPosFlights(String flightId,String flightName,String flightDate,String flightFrom,String flightTo,String paxCount,
                                 String businessClassPaxCount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO posFlights VALUES('"+flightId+"','"+flightName+"','"+flightDate+"','"+flightFrom+"'," +
                "'"+flightTo+"','"+paxCount+"','"+businessClassPaxCount+"');");
        db.close();
    }

    public String getSealList(String serviceType , String sealType){
        SQLiteDatabase db = this.getWritableDatabase();
        String seals = "";
        String sql;
        if(serviceType == null){
            sql = "select * from sealDetails where sealType = '"+sealType+"'";
    }
        else{
            sql = "select * from sealDetails where serviceType = '"+serviceType+"' and sealType = '"+sealType+"'";
        }
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String seal = cursor.getString(cursor.getColumnIndex("sealNo"));
                seals += seal + ",";
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        if(seals.isEmpty()) return "";
        return seals.substring(0,seals.length()-1);
    }

    public Map<String,Boolean> getSealVerifiedMap(String sealType,String serviceType){
        Map<String,Boolean> verifiedMap = new HashMap<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from sealDetails where serviceType = '"+serviceType+"' and sealType = '"+sealType+"'", null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String seal = cursor.getString(cursor.getColumnIndex("sealNo"));
                String verified = cursor.getString(cursor.getColumnIndex("isVerified"));
                verifiedMap.put(seal,"yes".equals(verified));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return verifiedMap;
    }

    public boolean isSealAlreadyUsed(String sealNo){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean isSealUsed = false;
        Cursor cursor = db.rawQuery("select sealNo from sealDetails where sealNo = '"+sealNo+"'", null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String seal = cursor.getString(cursor.getColumnIndex("sealNo"));
                if(seal.contains(sealNo)) isSealUsed = true;
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return isSealUsed;
    }

    public void updateSealTable(String sealNo ,String field, String value){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update sealDetails set "+field+" = '"+value+"'" +
                "where sealNo = '"+sealNo+"';");
        db.close();
    }

    public void deleteOutboundSeals(String serviceType){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from sealDetails where sealType = 'outbound' and serviceType = '"+serviceType+"';");
        db.close();
    }

    public void clearOrderSalesTables(String orderId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from dailySales where orderNumber ='"+orderId+"'");
        db.execSQL("delete from orderMainDetails where orderNumber ='"+orderId+"'");
        db.execSQL("delete from paymentMethods where orderNumber ='"+orderId+"'");
        db.execSQL("delete from creditCardDetails where orderNumber ='"+orderId+"'");
        db.close();
    }

    public void insertSealData(String sealType,String serviceType,String numOfSeals,String seal,String date,String flightName,String flightDate){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO sealDetails VALUES ('"+sealType+"','"+serviceType+"','"+numOfSeals+"','"+seal+"'," +
                "'"+date+"','"+flightName+"','"+flightDate+"','no')");
        db.close();
    }

    public void deleteSealsByAdmin(String sealType,String serviceType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from sealDetails where sealType = '" + sealType+"' and serviceType = '"+serviceType+"'");
        db.close();
    }

    public void insertDailySalesEntry(String orderNumber,String itemNo,String serviceType,String equipmentNo,String drawer,
                                      String quantity,String total,String buyerType,String sellerId,String date,String flightType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO dailySales VALUES('"+orderNumber+"','"+itemNo+"','"+equipmentNo+"','"+drawer+"','"
                +quantity+"','"+serviceType+"', '"+total+"','"+buyerType+"','"+sellerId+"','"+date+"','"+flightType+"');");
        db.close();
    }

    public void insertPaymentMethods(String orderNumber,String paymentType,String amount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO paymentMethods VALUES('"+orderNumber+"','"+paymentType+"','"+amount+"');");
        db.close();
    }

    public void updatePaymentMethods(String orderNumber,String paymentType,String amount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update paymentMethods set amount = '"+amount+"' where orderNumber = '"+orderNumber+"' " +
                "and paymentType = '"+paymentType+"';");
        db.close();
    }

    public void updateCreditCardDetails(String orderNumber,String amount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update creditCardDetails set amount = '"+amount+"' where orderNumber = '"+orderNumber+"';");
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

    public void insertOrderMainDetails(String orderNumber,String tax,String discount,String seatNo,String subTotal,String serviceType,String flightId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO orderMainDetails VALUES('"+orderNumber+"','"+tax+"','"+discount+"'" +
                ",'"+seatNo+"','"+subTotal+"','"+serviceType+"','"+flightId+"');");
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
                details.setSubTotal(cursor.getString(cursor.getColumnIndex("subTotal")));
                details.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                details.setFlightId(cursor.getString(cursor.getColumnIndex("flightId")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return details;
    }

    public List<OrderDetails> getOrders(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<OrderDetails> orderDetails = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from orderMainDetails"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                OrderDetails details = new OrderDetails();
                details.setOrderNumber(cursor.getString(cursor.getColumnIndex("orderNumber")));
                details.setTax(cursor.getString(cursor.getColumnIndex("tax")));
                details.setDiscount(cursor.getString(cursor.getColumnIndex("discount")));
                details.setSeatNo(cursor.getString(cursor.getColumnIndex("seatNo")));
                details.setSubTotal(cursor.getString(cursor.getColumnIndex("subTotal")));
                details.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                details.setFlightId(cursor.getString(cursor.getColumnIndex("flightId")));
                orderDetails.add(details);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return orderDetails;
    }

    public List<UserComment> getUserComments(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<UserComment> userComments = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from userComments"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                UserComment userComment = new UserComment();
                userComment.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                userComment.setFlightNo(cursor.getString(cursor.getColumnIndex("flightNo")));
                userComment.setFlightDate(cursor.getString(cursor.getColumnIndex("flightDate")));
                userComment.setArea(cursor.getString(cursor.getColumnIndex("area")));
                userComment.setComment(cursor.getString(cursor.getColumnIndex("comment")));
                userComments.add(userComment);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return userComments;
    }

    public List<PaymentMethods> getPaymentMethods(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<PaymentMethods> paymentMethods = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from paymentMethods"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                PaymentMethods details = new PaymentMethods();
                details.setOrderId(cursor.getString(cursor.getColumnIndex("orderNumber")));
                details.setPaymentType(cursor.getString(cursor.getColumnIndex("paymentType")));
                details.setAmount(cursor.getString(cursor.getColumnIndex("amount")));
                paymentMethods.add(details);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return paymentMethods;
    }

    public List<ItemSale> getItemSale(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<ItemSale> itemSales = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from dailySales"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                ItemSale details = new ItemSale();
                details.setOrderId(cursor.getString(cursor.getColumnIndex("orderNumber")));
                details.setItemId(cursor.getString(cursor.getColumnIndex("itemNo")));
                details.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                details.setPrice(cursor.getString(cursor.getColumnIndex("totalPrice")));
                details.setFlightType(cursor.getString(cursor.getColumnIndex("flightType")));
                itemSales.add(details);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return itemSales;
    }

    public List<ItemSale> getItemSale(String flightType){
        SQLiteDatabase db = this.getWritableDatabase();
        List<ItemSale> itemSales = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from dailySales where flightType = '"+flightType+"'"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                ItemSale details = new ItemSale();
                details.setOrderId(cursor.getString(cursor.getColumnIndex("orderNumber")));
                details.setItemId(cursor.getString(cursor.getColumnIndex("itemNo")));
                details.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                details.setPrice(cursor.getString(cursor.getColumnIndex("totalPrice")));
                details.setFlightType(cursor.getString(cursor.getColumnIndex("flightType")));
                details.setEquipmentType(cursor.getString(cursor.getColumnIndex("equipmentNo")));
                details.setDrawer(cursor.getString(cursor.getColumnIndex("drawer")));
                itemSales.add(details);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return itemSales;
    }

    public List<CreditCard> getCreditCardDetails(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<CreditCard> itemSales = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from creditCardDetails"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                CreditCard details = new CreditCard();
                details.setOrderId(cursor.getString(cursor.getColumnIndex("orderNumber")));
                details.setCreditCardNumber(cursor.getString(cursor.getColumnIndex("creditCardNumber")));
                details.setCardHolderName(cursor.getString(cursor.getColumnIndex("cardHolderName")));
                details.setExpireDate(cursor.getString(cursor.getColumnIndex("expireDate")));
                details.setPaidAmount(Float.parseFloat(cursor.getString(cursor.getColumnIndex("amount"))));
                itemSales.add(details);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return itemSales;
    }

    public List<POSFlight> getPOSFlightDetails(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<POSFlight> posFlightList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from posFlights"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                POSFlight posFlight = new POSFlight();
                posFlight.setFlightId(cursor.getString(cursor.getColumnIndex("flightId")));
                posFlight.setFlightName(cursor.getString(cursor.getColumnIndex("flightName")));
                posFlight.setFlightDate(cursor.getString(cursor.getColumnIndex("flightDate")));
                posFlight.setFlightFrom(cursor.getString(cursor.getColumnIndex("flightFrom")));
                posFlight.setFlightTo(cursor.getString(cursor.getColumnIndex("flightTo")));
                posFlight.seteClassPaxCount(cursor.getString(cursor.getColumnIndex("paxCount")));
                posFlight.setbClassPaxCount(cursor.getString(cursor.getColumnIndex("businessClassPaxCount")));
                posFlightList.add(posFlight);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return posFlightList;
    }

/*    public List<FAMessage> getFAMsgs(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<FAMessage> faDetailsList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from messageToBond"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                FAMessage faDetails = new FAMessage();
                faDetails.setFlightNo(cursor.getString(cursor.getColumnIndex("flightId")));
                faDetails.setMessageBody(cursor.getString(cursor.getColumnIndex("messageBody")));
                faDetails.setFlightDate(cursor.getString(cursor.getColumnIndex("flightDate")));
                faDetails.setFaName(cursor.getString(cursor.getColumnIndex("faName")));
                faDetailsList.add(faDetails);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return faDetailsList;
    }*/

    public List<FADetails> getFADetails(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<FADetails> faDetailsList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from faDetails"
                , null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                FADetails faDetails = new FADetails();
                faDetails.setFlightNo(cursor.getString(cursor.getColumnIndex("flightNo")));
                faDetails.setSector(cursor.getString(cursor.getColumnIndex("sector")));
                faDetails.setFlightDate(cursor.getString(cursor.getColumnIndex("flightDate")));
                faDetails.setFaName(cursor.getString(cursor.getColumnIndex("faName")));
                faDetailsList.add(faDetails);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return faDetailsList;
    }

    public void deleteFADetails(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from faDetails", null);
        if(res.getCount() > 0) {
            db.execSQL("delete from faDetails");
        }
        res.close();
        db.close();
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

    public boolean insertUserData(String xml){

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("users");
            JSONArray itemsArr = data.getJSONArray("user");
            List<User> comboDiscounts = gson.fromJson(itemsArr.toString(), new TypeToken<List<User>>(){}.getType());
            for(User user : comboDiscounts){
                db.execSQL("INSERT INTO Users VALUES('"+user.getUserName()+"','"+user.getPassword()+"');");
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /*public void insertFlightData(Context context){

        SQLiteDatabase db = this.getWritableDatabase();
        String sectorsStr = "";
        for(Flight flight : readFlightsXML(context)) {
            String sectors = "";
            if(flight.getSectorList() != null && !flight.getSectorList().isEmpty()){
                for(Sector sector : flight.getSectorList()){
                    sectors += sector.getFrom() +"+"+sector.getTo() + "*" +sector.getType()+ ",";
                }
                sectorsStr = sectors.substring(0,sectors.length()-1);
            }
            db.execSQL("INSERT INTO flights VALUES" +
                    "('"+flight.getFlightName()+"','"+flight.getFlightFrom()+"','"+flight.getFlightTo()+"','"+sectorsStr+"');");
        }
        db.close();
    }
*/
    public void insertFlightData(String xml) {

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            JSONObject jsonObj = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("flights");
            JSONArray itemsArr = data.getJSONArray("flight");
            List<FullFlight> flightList = gson.fromJson(itemsArr.toString(), new TypeToken<List<FullFlight>>() {
            }.getType());

            for (FullFlight flight : flightList) {
                if (flight.getFlightId() != null && !flight.getFlightId().isEmpty()) {

                    db.execSQL("INSERT INTO flights VALUES" +
                            "('" + flight.getFlightId() + "','" + flight.getObFlightName() + "'," +
                            "'" + flight.getObFrom() + "','" + flight.getObTo() + "','" + flight.getIbFlightName() + "'" +
                            ",'" + flight.getIbFrom() + "','" + flight.getIbTo() + "');");
                }
            }
            db.close();
        } catch (Exception e) {

        }
    }

    public void insertSectors(String xml) {

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            JSONObject jsonObj = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("sectors");
            JSONArray itemsArr = data.getJSONArray("sector");
            List<Sector> sectorList = gson.fromJson(itemsArr.toString(), new TypeToken<List<Sector>>() {
            }.getType());

            for (Sector sector : sectorList) {
                if (sector.getFlightNo() != null && !sector.getFlightNo().isEmpty()) {

                    db.execSQL("INSERT INTO sectors VALUES" +
                            "('" + sector.getFlightNo() + "','" + sector.getFrom() + "'," +
                            "'" + sector.getTo() + "','" + sector.getSectorType() + "','" + sector.getFlightType() + "');");
                }
            }
            db.close();
        } catch (Exception e) {

        }
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

    public void insertItemData(String xml){
        try {
            //File xml = new File(context.getFilesDir(), "item_list.xml");
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("Items");
            JSONArray itemsArr = data.getJSONArray("Item");
            List<Item> list = gson.fromJson(itemsArr.toString(), new TypeToken<List<Item>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(Item item : list){
                db.execSQL("INSERT INTO items VALUES" +
                        "('"+item.getItemCode()+"' ,'"+item.getItemName()+"','"+item.getItemHHC()+"'," +
                        "'"+item.getCategory()+"','"+item.getCatCode()+"','"+item.getCatlogNo()+"','"+item.getPrice()+"'," +
                        "'"+item.getPaxDiscPrice()+"','"+item.getStaffDiscPrice()+"','"+item.getDelist()+"'," +
                        "'"+item.getDfsrOrder()+"','"+item.getServiceType()+"','"+item.getScPrice()+"'," +
                        "'"+item.getBaseCurrency()+"','"+item.getBasePrice()+"','"+item.getSecondCurrency()+"'," +
                        "'"+item.getSecondPrice()+"','"+item.getActiveDate()+"','"+item.getWeight()+"','"+item.getNfcTag()+"');");
            }
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertKITList(String xml){
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("KITItems");
            JSONArray itemsArr = data.getJSONArray("KITItem");
            List<KITItem> kitList = gson.fromJson(itemsArr.toString(), new TypeToken<List<KITItem>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(KITItem item : kitList){
                if(item.getItemNo() != null && !item.getItemNo().isEmpty()) {
                    db.execSQL("INSERT INTO KITList VALUES" +
                            "('" + item.getEquipmentNo() + "','" + item.getItemNo() + "','" + item.getItemDescription() + "'," +
                            "'" + item.getQuantity() + "','" + item.getDrawer() + "','" + item.getQuantity() + "');");
                }
            }
            db.close();
            insertDrawerValidation();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertBondMessages(String xml){
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("BondMsgs");
            JSONArray itemsArr = data.getJSONArray("BondMsg");
            List<BondMessage> bondMessages = gson.fromJson(itemsArr.toString(), new TypeToken<List<BondMessage>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(BondMessage item : bondMessages){
                if(item.getMessageId() != null && !item.getMessageId().isEmpty()) {
                    db.execSQL("INSERT INTO bondMessages VALUES" +
                            "('" + item.getMessageId() + "','" + item.getMessageBody() + "');");
                }
            }
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<String> getBondMessages(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from bondMessages", null);
        List<String> msgs = new ArrayList<>();

        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
            msgs.add(cursor.getString(cursor.getColumnIndex("messageBody")));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return msgs;
    }

    public Item getItemFromNFCTag(String nfcTag){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from items where nfcTag = '"+nfcTag+"'", null);
        Item item = new Item();
        if (cursor.moveToFirst()){
           // while(!cursor.isAfterLast()){
                item.setItemCode(cursor.getString(cursor.getColumnIndex("itemNo")));
                item.setItemName(cursor.getString(cursor.getColumnIndex("itemName")));
                item.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                item.setCategory(cursor.getString(cursor.getColumnIndex("category")));
           // }
        }
        return item;
    }

    public List<KITItem> getAllKitItems(List<String> eqType){

        String equipmentNos = "";
        for(String str : eqType){
            equipmentNos += "'"+str+"',";
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from KITList where equipmentNo in ("+equipmentNos.substring(0,equipmentNos.length()-1)+")", null);
        List<KITItem> kitItems = new ArrayList<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                KITItem item = new KITItem();
                item.setEquipmentNo(cursor.getString(cursor.getColumnIndex("equipmentNo")));
                item.setDrawer(cursor.getString(cursor.getColumnIndex("drawer")));
                item.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                item.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                kitItems.add(item);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return kitItems;
    }

    public List<Item> getAllItems(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from Items", null);
        List<Item> items = new ArrayList<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                Item item = new Item();
                item.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                item.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                item.setItemName(cursor.getString(cursor.getColumnIndex("itemName")));
                item.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                items.add(item);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return items;
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

    public void resetDrawerValidation(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update drawerValidation set isValidated = 'No'");
        db.close();
    }

    public void insertKitListFromWS(String xml){
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("KITNumbers");
            JSONArray itemsArr = data.getJSONArray("KITNumber");
            List<KitNumber> kitNumbers = gson.fromJson(itemsArr.toString(), new TypeToken<List<KitNumber>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void insertKITNumbersList(String xml){
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("KITNumbers");
            JSONArray itemsArr = data.getJSONArray("KITNumber");
            List<KitNumber> kitNumbers = gson.fromJson(itemsArr.toString(), new TypeToken<List<KitNumber>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(KitNumber item : kitNumbers){
                if(item.getKitCode() != null && !item.getKitCode().isEmpty()) {
                    db.execSQL("INSERT INTO KITNumberList VALUES" +
                            "('" + item.getKitCode() + "','" + item.getKitDesc() + "','" + item.getServiceType() + "'," +
                            "'" + item.getActiveDate() + "','" + item.getNoOfEq() + "','" + item.getEquipment() + "');");
                }
            }
            db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean insertEquipmentTypeList(String xml){
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("Equipments");
            JSONArray itemsArr = data.getJSONArray("Equipment");
            List<Equipment> equipmentList = gson.fromJson(itemsArr.toString(), new TypeToken<List<Equipment>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(Equipment item : equipmentList){
                if(item.getEquipmentNo() != null && !item.getEquipmentNo().isEmpty()) {
                    db.execSQL("INSERT INTO equipmentType VALUES" +
                            "('" + item.getEquipmentNo() + "','" + item.getEquipmentDesc() + "','" + item.getEquipmentType() + "'," +
                            "'" + item.getDrawerPrefix() + "','" + item.getNoOfDrawers() + "','" + item.getNoOfSeals() + "');");
                }
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertCurrencyData(String xml,Context context){

        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("currencies");
            JSONArray itemsArr = data.getJSONArray("currency");
            List<Currency> currencyList = gson.fromJson(itemsArr.toString(), new TypeToken<List<Currency>>(){}.getType());
            SQLiteDatabase db = this.getWritableDatabase();
            for(Currency currency : currencyList){
                if(currency.getCurrencyCode() != null && !currency.getCurrencyCode().isEmpty()) {
                    if(currency.getPriorityOrder().equals("Base")){
                        SaveSharedPreference.setStringValues(context, Constants.SHARED_PREFERENCE_BASE_CURRENCY,currency.getCurrencyCode());
                    }
                    db.execSQL("INSERT INTO currency VALUES" +
                            "('" + currency.getCurrencyCode() + "','" + currency.getCurrencyDesc() + "','" + currency.getCurrencyRate() + "'," +
                            "'" + currency.getCurrencyType() + "','" + currency.getPriorityOrder() + "','" + currency.getEffectiveDate() + "');");
                }
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertPreOrderItems(String xml){
        try {
        JSONObject jsonObj  = XML.toJSONObject(xml);
        Gson gson = new Gson();
        JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("items");
        JSONArray itemsArr = data.getJSONArray("item");
        List<PreOrderItem> preOrders = gson.fromJson(itemsArr.toString(), new TypeToken<List<PreOrderItem>>(){}.getType());
        SQLiteDatabase db = this.getWritableDatabase();
            for(PreOrderItem item : preOrders){
                db.execSQL("INSERT INTO preOrderItems VALUES" +
                        "('"+item.getPreOrderId()+"','"+item.getItemNo()+"','"+item.getCategory()+"'," +
                        "'"+item.getQuantity()+"','Not Delivered','');");
            }

            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertPreOrders(String xml){
        try {

            Gson gson = new Gson();
            PreOrderMain preOrders = gson.fromJson(xml, PreOrderMain.class);
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("delete from preOrders");
            db.execSQL("delete from preOrderItems");
            for(PreOrder item : preOrders.getPreOrder()){
                db.execSQL("INSERT INTO preOrders VALUES" +
                        "('"+item.getPreOrderId()+"','"+item.getPNR()+"','"+item.getCustomerName()+"'," +
                        "'"+item.getServiceType()+"','Not Delivered','');");
                for(PreOrderItem preOrderItem : item.getItems()){
                    db.execSQL("INSERT INTO preOrderItems VALUES" +
                            "('"+preOrderItem.getPreOrderId()+"','"+preOrderItem.getItemNo()+"','"+preOrderItem.getCategory()+"'," +
                            "'"+preOrderItem.getQuantity()+"','Not Delivered','');");
                }
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
                        "('"+preOrder.getPreOrderId()+"','"+preOrder.getPNR()+"','"+preOrder.getCustomerName()+"'," +
                        "'"+preOrder.getServiceType()+"','Not Delivered','');");
            }
            //insert pre order items
            File orderItems = new File(context.getFilesDir(), "pre_order_items.xml");
            JSONObject orderItemsJOBj  = XML.toJSONObject(readStream(new FileInputStream(orderItems)));
            JSONObject mainTag = new JSONObject(orderItemsJOBj.toString()).getJSONObject("items");
            JSONArray itemList = mainTag.getJSONArray("item");
            List<PreOrderItem> itemList1 = gson.fromJson(itemList.toString(), new TypeToken<List<PreOrderItem>>(){}.getType());
            for(PreOrderItem item : itemList1){
                db.execSQL("INSERT INTO preOrderItems VALUES" +
                        "('"+item.getPreOrderId()+"','"+item.getItemNo()+"','"+item.getCategory()+"'," +
                        "'"+item.getQuantity()+"','Not Delivered','');");
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

    public boolean insertComboDiscount(String xml){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("comboDiscounts");
            JSONArray itemsArr = data.getJSONArray("comboDiscount");
            List<ComboDiscount> comboDiscounts = gson.fromJson(itemsArr.toString(), new TypeToken<List<ComboDiscount>>(){}.getType());
            for(ComboDiscount comboDiscount : comboDiscounts){
                if (comboDiscount.getComboId() != null && !comboDiscount.getComboId().isEmpty()) {
                    db.execSQL("INSERT INTO comboDiscounts VALUES" +
                            "('" + comboDiscount.getComboId() + "','" + comboDiscount.getDiscount() + "','" + comboDiscount.getItems() + "');");
                }
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertVoucherDetails(String xml){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            JSONObject jsonObj  = XML.toJSONObject(xml);
            Gson gson = new Gson();
            JSONObject data = new JSONObject(jsonObj.toString()).getJSONObject("vouchers");
            JSONArray itemsArr = data.getJSONArray("voucher");
            List<Voucher> voucherList = gson.fromJson(itemsArr.toString(), new TypeToken<List<Voucher>>(){}.getType());
            for(Voucher voucher : voucherList) {
                if (voucher.getVoucherId() != null && !voucher.getVoucherId().isEmpty()){
                    db.execSQL("INSERT INTO vouchers VALUES" +
                            "('" + voucher.getVoucherId() + "','" + voucher.getVoucherName() + "','" + voucher.getVoucherType() + "','" + voucher.getDiscount() + "');");
            }
            }
            db.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<PreOrderItem> getPreOrderItems(){
        List<PreOrderItem> preOrderItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from preOrderItems", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                PreOrderItem preOrderItem = new PreOrderItem();
                preOrderItem.setPreOrderId(cursor.getString(cursor.getColumnIndex("preOrderId")));
                preOrderItem.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                preOrderItem.setAdminStatus(cursor.getString(cursor.getColumnIndex("adminStatus")));
                preOrderItem.setDelivered(cursor.getString(cursor.getColumnIndex("delivered")));
                preOrderItems.add(preOrderItem);
                cursor.moveToNext();
            }
        }
        db.close();
        cursor.close();
        return preOrderItems;
    }

    public List<Voucher> getVouchers(){
        List<Voucher> voucherList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from vouchers", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Voucher voucher = new Voucher();
                voucher.setVoucherId(cursor.getString(cursor.getColumnIndex("voucherId")));
                voucher.setVoucherName(cursor.getString(cursor.getColumnIndex("voucherName")));
                voucher.setVoucherType(cursor.getString(cursor.getColumnIndex("voucherType")));
                voucher.setDiscount(cursor.getString(cursor.getColumnIndex("discount")));
                voucherList.add(voucher);
                cursor.moveToNext();
            }
        }
        db.close();
        cursor.close();
        return voucherList;
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
        db.close();
        cursor.close();
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
        db.close();
        cursor.close();
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

    public List<Flight> getFlightPairFromFlightName(String flightNo){
        List<Flight> flightList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from flights where obFlightName = '" + flightNo + "' or ibFlightName = '"+flightNo+"'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    Flight flight1 = new Flight();
                    flight1.setFlightId(cursor.getString(cursor.getColumnIndex("flightId")));
                    flight1.setFlightName(cursor.getString(cursor.getColumnIndex("obFlightName")));
                    flight1.setFlightFrom(cursor.getString(cursor.getColumnIndex("obFlightFrom")));
                    flight1.setFlightTo(cursor.getString(cursor.getColumnIndex("obFlightTo")));
                    flight1.setSectorList(getSectorsFromFlightId(cursor.getString(cursor.getColumnIndex("flightId")),"Outbound"));
                    flightList.add(flight1);

                    Flight flight2 = new Flight();
                    flight2.setFlightId(cursor.getString(cursor.getColumnIndex("flightId")));
                    flight2.setFlightName(cursor.getString(cursor.getColumnIndex("ibFlightName")));
                    flight2.setFlightFrom(cursor.getString(cursor.getColumnIndex("ibFlightFrom")));
                    flight2.setFlightTo(cursor.getString(cursor.getColumnIndex("ibFlightTo")));
                    flight2.setSectorList(getSectorsFromFlightId(cursor.getString(cursor.getColumnIndex("flightId")),"Inbound"));
                    flightList.add(flight2);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        catch (Exception e){
            db.close();
            return null;
        }
        db.close();
        return flightList;
    }

    public Flight getFlightFromFlightName(String flightName){
        Flight flight = new Flight();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from flights where obFlightName like '%"+flightName+"'", null);
            Cursor cursor1 = db.rawQuery("select * from flights where ibFlightName like '%"+flightName+"'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    flight.setFlightId(cursor.getString(cursor.getColumnIndex("flightId")));
                    flight.setFlightName(cursor.getString(cursor.getColumnIndex("obFlightName")));
                    flight.setFlightFrom(cursor.getString(cursor.getColumnIndex("obFlightFrom")));
                    flight.setFlightTo(cursor.getString(cursor.getColumnIndex("obFlightTo")));
                    flight.setSectorList(getSectorsFromFlightId(cursor.getString(cursor.getColumnIndex("flightId")),"Outbound"));
                    cursor.moveToNext();
                }
            }
            else if(cursor1.moveToFirst()){
                while(!cursor1.isAfterLast()){
                    flight.setFlightId(cursor1.getString(cursor.getColumnIndex("flightId")));
                    flight.setFlightName(cursor1.getString(cursor.getColumnIndex("ibFlightName")));
                    flight.setFlightFrom(cursor1.getString(cursor.getColumnIndex("ibFlightFrom")));
                    flight.setFlightTo(cursor1.getString(cursor.getColumnIndex("ibFlightTo")));
                    flight.setSectorList(getSectorsFromFlightId(cursor1.getString(cursor.getColumnIndex("flightId")),"Inbound"));
                    cursor1.moveToNext();
                }
            }
            else{
                cursor.close();
                cursor1.close();
                db.close();
                return null;
            }
            db.close();
            cursor.close();
            cursor1.close();
            return flight;
        }
        catch (Exception e){
            db.close();
            return null;
        }
    }

    public List<Sector> getSectorsFromFlightId(String flightId,String flightType){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Sector> sectors = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select * from sectors where flightNo = '"+flightId+"' and flightType = '"+flightType+"'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    Sector sector = new Sector();
                    sector.setFlightNo(cursor.getString(cursor.getColumnIndex("flightNo")));
                    sector.setFrom(cursor.getString(cursor.getColumnIndex("sectorFrom")));
                    sector.setTo(cursor.getString(cursor.getColumnIndex("sectorTo")));
                    sector.setSectorType(cursor.getString(cursor.getColumnIndex("sectorType")));
                    sector.setFlightType(cursor.getString(cursor.getColumnIndex("flightType")));
                    sectors.add(sector);
                    cursor.moveToNext();
                }
            }
            else{
                db.close();
                cursor.close();
                return null;
            }
            db.close();
            cursor.close();
            return sectors;
        }
        catch (Exception e){
            db.close();
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
        cursor.close();
        db.close();
    }

    public void updateDailySalesTable(String orderId,String itemId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from dailySales where orderNumber = '"+orderId+"' and itemNo = '"+itemId+"'");
        db.close();
    }

    public void updateOrderMainDetails(String orderId,String subTotal){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update orderMainDetails set subTotal = '"+subTotal+"' where orderNumber = '"+orderId+"'");
        db.close();
    }

    public List<String> getItemCatFromItems(String serviceType){
        String packTypes = getEquipmentsFromKitCodes(serviceType);
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> categoryList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select distinct category from items where itemNo in " +
                    "( select itemNo from  KITList where  equipmentNo in ("+packTypes+")"
                    , null);
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

    public String getEquipmentsFromKitCodes(String serviceType){
        String eqTypeStr = "";
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select equipment from KITNumberList where serviceType = '"+serviceType+"'", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    eqTypeStr += "'" + cursor.getString(cursor.getColumnIndex("equipment")).replace(",","','") +"',";
                    cursor.moveToNext();
                }
            }
            else{
                return null;
            }
            db.close();
            cursor.close();
            return eqTypeStr.substring(0,eqTypeStr.length()-1);
        }
        catch (Exception e){
            db.close();
            return null;
        }
    }

    public String getEquipmentsFromKitCode(String kitCode){
        String eqTypeStr = "";
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select equipment from KITNumberList where kitCode in ("+kitCode+")", null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    eqTypeStr += "'" + cursor.getString(cursor.getColumnIndex("equipment")).replace(",","','") +"',";
                    cursor.moveToNext();
                }
            }
            else{
                return null;
            }
            db.close();
            cursor.close();
            return eqTypeStr.substring(0,eqTypeStr.length()-1);
        }
        catch (Exception e){
            db.close();
            return null;
        }
    }

    public List<String> getItemCatFromServiceType(String serviceType){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> categoryList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("select distinct category from items where " +
                    "serviceType = '"+serviceType+"'", null);
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

    public List<PreOrderItem> getPreOrderItemsFromPreOrderId(String preOrderId,String userMode){
        SQLiteDatabase db = this.getReadableDatabase();
        List<PreOrderItem> itemList = new ArrayList<>();
        try {
            Cursor cursor;
            if(userMode.equals("admin")) {
                cursor = db.rawQuery("select * from preOrderItems where " +
                        "preOrderId = '" + preOrderId + "'", null);
            }
            else{
                cursor = db.rawQuery("select * from preOrderItems where " +
                        "preOrderId = '" + preOrderId + "' and adminStatus = 'Loaded'", null);
            }
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    PreOrderItem item = new PreOrderItem();
                    item.setPreOrderId(cursor.getString(cursor.getColumnIndex("preOrderId")));
                    item.setItemNo(cursor.getString(cursor.getColumnIndex("itemNo")));
                    item.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                    item.setQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
                    item.setAdminStatus(cursor.getString(cursor.getColumnIndex("adminStatus")));
                    item.setDelivered(cursor.getString(cursor.getColumnIndex("delivered")));
                    itemList.add(item);
                    cursor.moveToNext();
                }
            }
            db.close();
            cursor.close();
            return itemList;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Map<String,List<PreOrder>> getAvailablePreOrders(String mode){

        Map<String,List<PreOrder>> serviceTypePreOrderMap = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor;
            if("admin".equals(mode)) {
                cursor = db.rawQuery("select * from preOrders", null);
            }
            else{
                cursor = db.rawQuery("select * from preOrders where preOrderId in " +
                        "(select distinct preOrderId from preOrderItems where adminStatus = 'Loaded')", null);
            }
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    PreOrder preOrder = new PreOrder();
                    preOrder.setPNR(cursor.getString(cursor.getColumnIndex("PNR")));
                    preOrder.setCustomerName(cursor.getString(cursor.getColumnIndex("customerName")));
                    preOrder.setPreOrderId(cursor.getString(cursor.getColumnIndex("preOrderId")));
                    String serviceType = cursor.getString(cursor.getColumnIndex("serviceType"));
                    preOrder.setServiceType(cursor.getString(cursor.getColumnIndex("serviceType")));
                    preOrder.setDelivered(cursor.getString(cursor.getColumnIndex("delivered")));
                    preOrder.setAdminStatus(cursor.getString(cursor.getColumnIndex("adminStatus")));

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

    public void updatePreOrderDeliveryStatus(String deliveryStatus,String itemId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update preOrders set delivered = '"+deliveryStatus+"' where preOrderId = '"+itemId+"';");
        db.execSQL("update preOrderItems set delivered = '"+deliveryStatus+"' where preOrderId = '"+itemId+"' " +
                "and adminStatus = 'Loaded';");
        db.close();
    }

    public void updatePreOrderAdminStatus(String adminStatus,String preOrderId,String itemId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("update preOrderItems set adminStatus = '"+adminStatus+"' where  preOrderId = '"+preOrderId+"' and " +
                "itemNo = '"+itemId+"';");
        db.close();
    }

    public List<SoldItem> getItemListFromItemCategory(String category,String kitCodes){
        String packTypes = getEquipmentsFromKitCode(kitCodes);
        SQLiteDatabase db = this.getReadableDatabase();
        List<SoldItem> itemList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("SELECT items.itemNo as itemNo,items.itemName as itemName," +
                    "items.price as price,KITList.equipmentNo as equipmentNo," +
                    "KITList.drawer as drawer FROM (SELECT * FROM items where category = '"+category+"') as items INNER JOIN " +
                    "(SELECT * FROM KITList WHERE equipmentNo in ("+packTypes+")) " +
                    "as KITList ON items.itemNo = KITList.itemNo", null);

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

    public Map<String,List<String>> getServiceTypeKitCodesMap(List<String> kitCodes){

        Map<String,List<String>> map = new HashMap<>();
        for(String kitCode : kitCodes){
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT serviceType from KITNumberList where kitCode = '"+kitCode+"'" ,
                    null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()) {
                    String serviceType = cursor.getString(cursor.getColumnIndex("serviceType"));
                    if(map.containsKey(serviceType)){
                        map.get(serviceType).add(kitCode);
                    }
                    else{
                        List<String> kitCodesList = new ArrayList<>();
                        kitCodesList.add(kitCode);
                        map.put(serviceType,kitCodesList);
                    }
                    cursor.moveToNext();
                }
            }

        }
        return map;
    }

    public List<SoldItem> getItemListFromItemCategoryForPreOrder(String category,String serviceType){
        SQLiteDatabase db = this.getReadableDatabase();
        List<SoldItem> itemList = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("SELECT * from items where category = '"+category+"' " +
                    "and serviceType = '"+serviceType+"'", null);

            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    SoldItem item = new SoldItem();
                    item.setItemId(cursor.getString(cursor.getColumnIndex("itemNo")));
                    item.setItemDesc(cursor.getString(cursor.getColumnIndex("itemName")));
                    item.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                    item.setItemCategory(cursor.getString(cursor.getColumnIndex("category")));
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

    public List<String> getItemCodesList(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select itemNo from items",null);
        List<String> itemCodes = new ArrayList<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                itemCodes.add(cursor.getString(cursor.getColumnIndex("itemNo")));
                cursor.moveToNext();
            }
        }
        return itemCodes;
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

    public String getNoOfSealsFromKitCodes(List<String> kitCodeList){
        int count = 0;
        String kitCodes = "";
        for(String str : kitCodeList){
            kitCodes += "'"+str+"',";
        }
        String packTypes = getEquipmentsFromKitCode(kitCodes.substring(0,kitCodes.length()-1));
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select noOfSeals from equipmentType where equipmentNo in ("+packTypes+")",null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String countField = cursor.getString(cursor.getColumnIndex("noOfSeals"));
                count += Integer.parseInt(countField);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return count+"";
    }

    public List<String> getEquipmentTypesList(List<String> kitCode){
        String kitCodes = "";
        List<String> kitCodesList = new ArrayList<>();
        for(String str : kitCode){
            kitCodes += "'"+str+"',";
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from KITNumberList where kitCode " +
                "in ("+kitCodes.substring(0,kitCodes.length()-1)+")",null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String countField = cursor.getString(cursor.getColumnIndex("equipment"));
                if(countField.contains(",")){
                    kitCodesList.addAll(Arrays.asList(countField.split(",")));
                }
                else {
                    kitCodesList.add(countField);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        //}
        return kitCodesList;
    }

    public String getKitNumberListCountValueFromKitCodes(List<String> kitCode,String fieldVal){
        int count = 0;
        String kitCodes = "";
        for(String str : kitCode){
            kitCodes += "'"+str+"',";
        }

        //for(String kitCodeStr : kitCode){
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("select "+fieldVal+" from KITNumberList where kitCode " +
                    "in ("+kitCodes.substring(0,kitCodes.length()-1)+")",null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    String countField = cursor.getString(cursor.getColumnIndex(fieldVal));
                    count += Integer.parseInt(countField);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
        //}
        return count+"";
    }

    public Map<String,Map<String,List<KITItem>>> getDrawerKitItemMapFromServiceType(String kitCode){
        String packTypes = getEquipmentsFromKitCode(kitCode);
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String,Map<String,List<KITItem>>> drawerKitItemMap = new HashMap<>();
        try {
            Cursor cursor = db.rawQuery("select * from  KITList where  equipmentNo in ("+packTypes+" )", null);
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
                equipment.setNoOfSeals(cursor.getString(cursor.getColumnIndex("noOfSeals")));
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
                kitNumber.setEquipment(cursor.getString(cursor.getColumnIndex("equipment")));
                kitCodes.add(kitNumber);
                cursor.moveToNext();
            }
        }
        db.close();
        cursor.close();
        return kitCodes;
    }

    public void insertAcceptPreOrders(List<SoldItem> items,AcceptPreOrder preOrder){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO acceptPreOrder VALUES('" + preOrder.getOrderNumber() + "'" +
                ",'" + preOrder.getPaxName() + "','" + preOrder.getFlightNumber() + "'," +
                "'"+preOrder.getFlightDate()+"','"+preOrder.getFlightSector()+"','"+preOrder.getPnr()+"','"+preOrder.getServiceType()+"','"+preOrder.getAmount()+"');");
        for(SoldItem item : items) {
            db.execSQL("INSERT INTO acceptPreOrderItems VALUES('" + preOrder.getOrderNumber() + "'" +
                    ",'" + item.getItemId() + "','" + item.getItemCategory() + "','"+item.getQuantity()+"');");
        }
        db.close();
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

    /*private List<Flight> readFlightsXML(Context context){
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
    }*/

    /*private List<Flight> readFlightsXMLString(String xml){
        List<Flight> flights = new ArrayList<>();
        try {
            Document doc = POSCommonUtils.loadXMLFromString(xml);
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
    }*/

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

    /*private static List<Sector> getSectors(Element element) {
        List<Sector> sectors = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName("sector");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element2 = (Element) node;
                Sector sector = new Sector();
                sector.setFrom(getValue("from", element2));
                sector.setTo(getValue("to", element2));
                sector.setType(getValue("type", element2));
                sectors.add(sector);
            }
        }
        return sectors;
    }*/
}
