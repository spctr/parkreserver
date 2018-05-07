package com.example.alpertunga.parkreserver;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.example.alpertunga.parkreserver.MainActivity.SERVERPORT;
import static com.example.alpertunga.parkreserver.MainActivity.SERVER_IP;
import static com.example.alpertunga.parkreserver.MainActivity.getArg;

public class ParkPlacesActivity extends AppCompatActivity {
    ListView vehiclesView;
    String buildingId="";
    private Socket socket;
    private String[] parkIds;
    private String[] vehiclePlates;
    private String[] driverNames;
    private String[] owners;
    EditText driverName;
    EditText plateNumber;
    Button addVehicle;
    SwipeRefreshLayout swipeRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_places);
        Intent intent=getIntent();
        buildingId=intent.getStringExtra("BuildingID");
        vehiclesView=findViewById((R.id.listView2));
        driverName=findViewById(R.id.editText5);
        plateNumber=findViewById(R.id.editText6);
        addVehicle=findViewById(R.id.button2);
        swipeRefresh=findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new IOOperation().execute("listVehicles,"+buildingId+",");
                        swipeRefresh.setRefreshing(false);
                    }
                }
        );
        addVehicle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {

                String ownerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String plateNo = plateNumber.getText().toString();
                String driver = driverName.getText().toString();
                String query = "createVehicle," + buildingId + "," + plateNo + "," + ownerId + "," + driver + ",";
                System.out.println(query);
                if(plateNo.indexOf(',')!=-1||driver.indexOf(',')!=-1)return;
                new IOOperation().execute(query);
            }
        });
        vehiclesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                removeVehicle(parkIds[position],owners[position]);
            }
        });
        new IOOperation().execute("listVehicles,"+buildingId+",");

    }
    private void removeVehicle(String parkId,String ownerId){
        System.out.println(ownerId+" "+Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        if(ownerId.equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))){
            String query="removeVehicle,"+buildingId+","+parkId+",";
            System.out.println(query);
            new IOOperation().execute(query);
        }

    }
    private void setListView(String vehicleListInput){
        String[] buildingList=new String[]{vehicleListInput};
        if(!getArg(buildingList).equals("vehicles")){
           //Error message
            String error=getArg(buildingList);
            Context context=getApplicationContext();
            Toast toast= Toast.makeText(context,error, Toast.LENGTH_SHORT);
            toast.show();
            if(!error.equals("You can not add anymore vehicles")){
                Intent intent=new Intent(this, MainActivity.class);
                startActivity(intent);
                return;
            }else return;

        }

        int size=Integer.parseInt(getArg(buildingList));
        if(size==0){
            Intent intent=new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        parkIds=new String[size];
        owners=new String[size];
        vehiclePlates=new String[size];
        driverNames=new String[size];
        for(int i=0;i<size;i++){
            vehiclePlates[i]=getArg(buildingList);
            owners[i]=getArg(buildingList);
            driverNames[i]=getArg(buildingList);
            vehiclePlates[i]=vehiclePlates[i]+", Driver: "+driverNames[i];
            parkIds[i]=getArg(buildingList);
        }
        ArrayAdapter adapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1,vehiclePlates);
        vehiclesView.setAdapter(adapter);
    }
    class IOOperation extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String command=strings[0];
            String result="";
            try {

                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);

                System.out.println(command);
                pw.println(command);
                result=br.readLine();
                System.out.println("result:"+result);

                br.close();
                pw.close();
                socket.close();
            }catch(UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return result;
        }
        protected void onPostExecute(String result){
            setListView(result);
        }
    }
}
