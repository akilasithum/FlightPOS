package com.pos.flightpos;

import android.app.AlertDialog;
import android.content.Context;
import android.pt.nfc.Nfc;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class NFCReader extends AppCompatActivity {

    Nfc nfc;
    EditText et_recv;
    ImageButton nfcBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcreader);
        et_recv = findViewById(R.id.et_recv);
        nfcBtn = findViewById(R.id.nfcBtn);
        nfcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readNFC();
            }
        });
    }

    private void readNFC(){
        nfc = new Nfc();
        int ret = nfc.open();
        if (ret == 0)
        {
            Toast("open success");
        }
        else {
            Toast("open fail");
            return;
        }
        int ret2 = nfc.activate();
        if (ret2 == 0) {
            Toast("active success");
        }
        else  {
            Toast("active fail");
            return;
        }

        byte[] data = new byte[1024];
        int ret1 = nfc.seek(data);
        if (ret1<0) {
            Toast("not find nfc");
            return;
        }
        else {
            String string1 ="" ;
            for (int i = 0; i < 4; i++) {
                string1 += Integer.toHexString(data[i]&0xff)+",";
            }
            Messagebox("uid:"+string1);
        }
        sent_cmd();
    }

    public void sent_cmd()
    {
        String sent_str = "00,a4,04,00,0e,32,50,41,59,2e,53,59,53,2e,44,44,46,30,31,00";

        if (sent_str.equals("")) {
            Messagebox("please input apdu cmd");
            return;
        }

        String cmd_str[] = sent_str.split(",");
        byte[] sent_byte = new byte[cmd_str.length];
        for (int i = 0; i < sent_byte.length; i++) {
            sent_byte[i] = (byte) Integer.parseInt(cmd_str[i], 16);
        }

        byte[] data = new byte[1024];
        int len = nfc.exeAPDU(sent_byte,sent_byte.length, data);

        if (len<=0)
        {
            Toast(" exe fail");
            return;
        }
        String str_recv= new String();
        for (int i = 0; i < len; i++) {
            str_recv += Integer.toHexString(data[i]&0xff)+",";
        }
        et_recv.setText(str_recv);

    }

    public void Toast( String info)
    {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    public void Messagebox(String info)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("title");
        builder.setMessage(info);
        builder.setPositiveButton("yes", null);
        builder.show();
    }
}
