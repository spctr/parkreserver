package com.example.alpertunga.parkreserver;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {
    private ListView buildingsView;
    private int[] buildingIds;
    private String[] buildingNames;
    private Socket socket;
    public static final int SERVERPORT = 5000;
    public static final String SERVER_IP="192.168.0.241";
    Button createButton;
    EditText editBuilding;
    EditText editPlate;
    EditText editDriver;
    EditText editParks;
    SwipeRefreshLayout swipeRefresher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildingsView=findViewById(R.id.listView);
        createButton=findViewById(R.id.button);
        editBuilding=findViewById(R.id.editText);
        editParks=findViewById(R.id.editText2);
        editPlate=findViewById(R.id.editText4);
        editDriver=findViewById(R.id.editText3);
        swipeRefresher=findViewById(R.id.swiperefresh2);
        swipeRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new IOOperation().execute("listBuildings,");
                swipeRefresher.setRefreshing(false);
            }
        });
        createButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                String buildingName = editBuilding.getText().toString();
                String ownerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String plateNumber = editPlate.getText().toString();
                int parkCount = Integer.parseInt(editParks.getText().toString());
                String driverName = editDriver.getText().toString();
                String query = "createBuilding," + buildingName + "," + ownerId + "," + plateNumber + "," + parkCount + "," + driverName+",";
                System.out.println(query);
                if(buildingName.indexOf(',')!=-1||plateNumber.indexOf(',')!=-1||driverName.indexOf(',')!=-1||parkCount<1)return;
                new IOOperation().execute(query);
            }
        });
        buildingsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                openBuilding(buildingIds[position]);
            }
        });
        new IOOperation().execute("listBuildings,");
    }

    public void openBuilding(int buildingID){
        Intent intent=new Intent(this,ParkPlacesActivity.class);
        intent.putExtra("BuildingID",""+buildingID);
        startActivity(intent);
    }
    private void setListView(String buildingListInput){
        String[] buildingList=new String[]{buildingListInput};
        if(!getArg(buildingList).equals("buildings")){
            System.out.println("not a building string");
            return;
        }
        int size=Integer.parseInt(getArg(buildingList));
        buildingIds=new int[size];
        buildingNames=new String[size];
        for(int i=0;i<size;i++){
            buildingIds[i]=Integer.parseInt(getArg(buildingList));
            buildingNames[i]=getArg(buildingList);
        }
        ArrayAdapter adapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1, buildingNames);
        buildingsView.setAdapter(adapter);
    }
    public static String getArg(String[] command){
        System.out.println(command[0]);
        String firstArg=command[0].substring(0,command[0].indexOf(','));
        if(command[0].indexOf(',')+1<command[0].length())command[0]=command[0].substring(command[0].indexOf(',')+1,command[0].length());
        else command[0]="";
        return firstArg;
    }
    class IOOperation extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            String command=strings[0];
            String result="";
            try {
                System.out.println("1");
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
                System.out.println("2");
                System.out.println(command);
                pw.println(command);
                result=br.readLine();
                System.out.println("result:"+result);
                System.out.println("3");
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
