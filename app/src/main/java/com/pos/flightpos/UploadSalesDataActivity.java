package com.pos.flightpos;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pos.flightpos.objects.Constants;
import com.pos.flightpos.objects.CreditCard;
import com.pos.flightpos.objects.OrderDetails;
import com.pos.flightpos.objects.XMLMapper.FADetails;
import com.pos.flightpos.objects.XMLMapper.ItemSale;
import com.pos.flightpos.objects.XMLMapper.POSFlight;
import com.pos.flightpos.objects.XMLMapper.PaymentMethods;
import com.pos.flightpos.objects.XMLMapper.SIFDetails;
import com.pos.flightpos.utils.HttpHandler;
import com.pos.flightpos.utils.POSCommonUtils;
import com.pos.flightpos.utils.POSDBHandler;
import com.pos.flightpos.utils.SaveSharedPreference;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class UploadSalesDataActivity extends AppCompatActivity {
    POSDBHandler posdbHandler;
    ProgressDialog dia;
    List<String> resultList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_sales_data);
        posdbHandler = new POSDBHandler(this);
        dia = new ProgressDialog(this);
        dia.setTitle("Upload");
        dia.setMessage("Sales data upload in progress. Please wait...");
        dia.show();
        new GetContacts().execute();
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler handler = new HttpHandler();
            resultList = new ArrayList<>();
            resultList.add(handler.postRequest(getSIFDetailsXML(),"sifDetails"));
            resultList.add(handler.postRequest(getOrderMailDetailsXML(),"orderMainDetails"));
            resultList.add(handler.postRequest(getPaymentMethodsXML(),"paymentMethods"));
            resultList.add(handler.postRequest(getItemSaleXML(),"itemSales"));
            resultList.add(handler.postRequest(getCreditCardXML(),"creditCardDetails"));
            resultList.add(handler.postRequest(getFlightDetailsXML(),"posFlightDetails"));
            resultList.add(handler.postRequest(getFADetailsXML(),"faDetails"));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(resultList.size() == 7){
                dia.cancel();
                posdbHandler.clearDailySalesTable();
                showMsgAndExit("Sync Completed","POS data update completed. Click ok to continue",true);
            }
            else{
                dia.cancel();
                showMsgAndExit("Something wrong","Some table may not updated correctly",false);
            }
        }
    }

    private void showMsgAndExit(String header,String msg,final boolean isSuccess){
        new AlertDialog.Builder(this)
                .setTitle(header)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        Intent intent = new Intent(UploadSalesDataActivity.this, MainActivity.class);
                        if(isSuccess) {
                            intent.putExtra("parent", "UploadSalesDataActivity");
                        }
                        else{
                            intent.putExtra("parent","SelectModeActivity");
                        }
                        startActivity(intent);

                    }})
                .show();
    }

    private String getSIFDetailsXML(){
        String deviceId = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_DEVICE_ID);
        String sifNo = SaveSharedPreference.getStringValues(this, Constants.SHARED_PREFERENCE_SIF_NO);
        SIFDetails sif = posdbHandler.getSIFDetails(sifNo);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("sifDetails");
        root.addElement("sifNo").addText(sifNo);
        root.addElement("deviceId").addText(deviceId);
        root.addElement("packedFor").addText(sif.getPackedFor());
        root.addElement("packedTime").addText(sif.getPackedTime());
        root.addElement("crewOpenedTime").addText(sif.getCrewOpenedTime());
        root.addElement("crewClosedTime").addText(sif.getCrewClosedTime());
        return document.asXML();
    }

    private String getOrderMailDetailsXML(){
        List<OrderDetails> orderMainDetailsList = posdbHandler.getOrders();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("orderMainDetails");
        for(OrderDetails mainDetails : orderMainDetailsList){
            Element orderMainDetail = root.addElement("orderMainDetail");
            orderMainDetail.addElement("orderId").addText(mainDetails.getOrderNumber());
            orderMainDetail.addElement("tax").addText(mainDetails.getTax());
            orderMainDetail.addElement("discount").addText(mainDetails.getDiscount());
            orderMainDetail.addElement("seatNo").addText(mainDetails.getSeatNo());
            orderMainDetail.addElement("subTotal").addText(mainDetails.getSubTotal());
            orderMainDetail.addElement("serviceType").addText(mainDetails.getServiceType());
            orderMainDetail.addElement("flightId").addText(mainDetails.getFlightId());
        }
        if(orderMainDetailsList.size() == 1){
            Element orderMainDetail = root.addElement("orderMainDetail");
            orderMainDetail.addElement("orderId").addText("");
            orderMainDetail.addElement("tax").addText("");
            orderMainDetail.addElement("discount").addText("");
            orderMainDetail.addElement("seatNo").addText("");
            orderMainDetail.addElement("subTotal").addText("");
            orderMainDetail.addElement("serviceType").addText("");
            orderMainDetail.addElement("flightId").addText("");
        }
        return document.asXML();
    }

    private String getPaymentMethodsXML(){

        List<PaymentMethods> paymentMethods = posdbHandler.getPaymentMethods();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("paymentMethods");
        for(PaymentMethods methods : paymentMethods){
            Element orderMainDetail = root.addElement("paymentMethod");
            orderMainDetail.addElement("orderId").addText(methods.getOrderId());
            orderMainDetail.addElement("paymentType").addText(methods.getPaymentType());
            orderMainDetail.addElement("amount").addText(methods.getAmount());
        }
        if(paymentMethods.size() == 1){
            Element dummy = root.addElement("paymentMethod");
            dummy.addElement("orderId").addText("");
            dummy.addElement("paymentType").addText("");
            dummy.addElement("amount").addText("");
        }
        return document.asXML();
    }

    private String getItemSaleXML(){

        List<ItemSale> itemSaleList = posdbHandler.getItemSale();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("itemSales");
        for(ItemSale itemSale : itemSaleList){
            Element orderMainDetail = root.addElement("itemSale");
            orderMainDetail.addElement("orderId").addText(itemSale.getOrderId());
            orderMainDetail.addElement("itemId").addText(itemSale.getItemId());
            orderMainDetail.addElement("quantity").addText(itemSale.getQuantity());
            orderMainDetail.addElement("price").addText(itemSale.getPrice());
        }
        if(itemSaleList.size() == 1){
            Element dummy = root.addElement("itemSale");
            dummy.addElement("orderId").addText("");
            dummy.addElement("itemId").addText("");
            dummy.addElement("quantity").addText("");
            dummy.addElement("price").addText("");
        }
        return document.asXML();
    }

    private String getCreditCardXML(){

        List<CreditCard> creditCardDetails = posdbHandler.getCreditCardDetails();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("creditCards");
        for(CreditCard creditCard : creditCardDetails){
            Element orderMainDetail = root.addElement("creditCard");
            orderMainDetail.addElement("orderId").addText(creditCard.getOrderId());
            orderMainDetail.addElement("creditCardNumber").addText(creditCard.getCreditCardNumber());
            orderMainDetail.addElement("cardHolderName").addText(creditCard.getCardHolderName().trim());
            orderMainDetail.addElement("expireDate").addText(creditCard.getExpireDate());
            orderMainDetail.addElement("amount").addText(String.valueOf(creditCard.getPaidAmount()));
        }
        if(creditCardDetails.size() == 1){
            Element dummy = root.addElement("creditCard");
            dummy.addElement("orderId").addText("");
            dummy.addElement("creditCardNumber").addText("");
            dummy.addElement("cardHolderName").addText("");
            dummy.addElement("expireDate").addText("");
            dummy.addElement("amount").addText("");
        }
        return document.asXML();
    }

    private String getFlightDetailsXML(){
        List<POSFlight> posFlightList = posdbHandler.getPOSFlightDetails();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("flights");
        for(POSFlight posFlight : posFlightList){
            Element orderMainDetail = root.addElement("flight");
            orderMainDetail.addElement("flightId").addText(posFlight.getFlightId());
            orderMainDetail.addElement("flightName").addText(posFlight.getFlightName());
            orderMainDetail.addElement("flightDate").addText(posFlight.getFlightDate());
            orderMainDetail.addElement("flightFrom").addText(posFlight.getFlightFrom());
            orderMainDetail.addElement("flightTo").addText(posFlight.getFlightTo());
            orderMainDetail.addElement("eClassPaxCount").addText(posFlight.geteClassPaxCount());
            orderMainDetail.addElement("bClassPaxCount").addText(posFlight.getbClassPaxCount());
            String sifNo = SaveSharedPreference.getStringValues(this,Constants.SHARED_PREFERENCE_SIF_NO);
            orderMainDetail.addElement("sifNo").addText(sifNo);
        }
        if(posFlightList.size() == 1){
            Element orderMainDetail = root.addElement("flight");
            orderMainDetail.addElement("flightId").addText("");
            orderMainDetail.addElement("flightName").addText("");
            orderMainDetail.addElement("flightDate").addText("");
            orderMainDetail.addElement("flightFrom").addText("");
            orderMainDetail.addElement("flightTo").addText("");
            orderMainDetail.addElement("eClassPaxCount").addText("");
            orderMainDetail.addElement("bClassPaxCount").addText("");
            orderMainDetail.addElement("sifNo").addText("");
        }
        return document.asXML();
    }

    private String getFADetailsXML(){
        List<FADetails> posFlightList = posdbHandler.getFADetails();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("faDetails");
        for(FADetails posFlight : posFlightList){
            Element orderMainDetail = root.addElement("fa");
            orderMainDetail.addElement("flightNo").addText(posFlight.getFlightNo());
            orderMainDetail.addElement("sector").addText(posFlight.getSector());
            orderMainDetail.addElement("flightDate").addText(posFlight.getFlightDate());
            orderMainDetail.addElement("faName").addText(posFlight.getFaName());
        }
        if(posFlightList.size() == 1){
            Element orderMainDetail = root.addElement("flight");
            orderMainDetail.addElement("flightNo").addText("");
            orderMainDetail.addElement("sector").addText("");
            orderMainDetail.addElement("flightDate").addText("");
            orderMainDetail.addElement("faName").addText("");
        }
        return document.asXML();
    }
}
