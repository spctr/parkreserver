package com.company;


import java.util.ArrayList;

public class Building {
    private static ArrayList<Building> allBuildings=new ArrayList<>();
    private ArrayList<ParkPlace> vehicles;
    private String buildingName;
    private int buildingId;
    private static int buildingCounter=0;
    private int parkLimit;
    private Building(String buildingName, String plateNumber,String owner, int parkLimit,String driverName){
        vehicles=new ArrayList<>();
        buildingCounter++;
        buildingId=buildingCounter;
        this.buildingName=buildingName;
        this.parkLimit=parkLimit;
        //System.out.println(buildingName+plateNumber+owner+parkLimit+driverName);
        ParkPlace firstVehicle=new ParkPlace(plateNumber,owner,driverName,1);
        vehicles.add(firstVehicle);
    }

    public static void addBuilding(String buildingName, String owner, String plateNumber, int parkLimit,String driverName){
        synchronized (Building.class){
            Building newBuilding=new Building(buildingName,plateNumber,owner,parkLimit,driverName);
            allBuildings.add(newBuilding);
        }
    }

    private synchronized Building addVehicle(String plateNumber, String owner,String driverName){

        if(ownerExists(owner)){
            System.out.println("owner has a vehicle");
            return null;
            //this owner already has a vehicle
        }
        if(vehicles.size()==parkLimit){

            System.out.println("building is full");
            //this building is full

         return null;
        }else{
            vehicles.add(new ParkPlace(plateNumber,owner,driverName,nextEmptyPlace()));
        }
        return this;
    }
    private synchronized boolean ownerExists(String owner){
        for(ParkPlace park:vehicles){
            if(owner.equals(park.getOwner()))return true;
        }
        return false;
    }
    private synchronized int nextEmptyPlace(){
        int empty=0;
        for(int i=1;i<parkLimit+1;i++){
            boolean thereIsAMatch=false;
            for(ParkPlace park:vehicles){
                if(i==park.getParkId()){
                    thereIsAMatch=true;
                    break;
                }
            }
            if(!thereIsAMatch)return i;
        }
        return empty;
    }
    private synchronized String listVehicles(){
        String list="vehicles,"+vehicles.size()+",";

        for(ParkPlace vehicle : vehicles){
            list+=vehicle.getVehicle();
        }
        return list;
    }
    public static String listBuildings(){
        String list="buildings,";
        synchronized (Building.class){
            list+=allBuildings.size()+",";
            for(Building building:allBuildings){
                list+=building.buildingId+","+building.buildingName+",";
            }
        }
        //System.out.println(list);
        return list;
    }


    private synchronized void removeVehicle(int parkId){
            ParkPlace toRemove=getVehicle(parkId);
            if(toRemove==null)return;
            vehicles.remove(toRemove);
            if(vehicles.isEmpty()){
                removeBuilding(buildingId);
            }
    }
    private static void removeBuilding(int buildingId){
        synchronized (Building.class){
            allBuildings.remove(getBuilding(buildingId));
        }
    }
    private ParkPlace getVehicle(int parkId){

        for(ParkPlace vehicle:vehicles){
            if(vehicle.getParkId()==parkId){
                return vehicle;
            }
        }
        return null;
    }
    private static Building getBuilding(int buildingId){
        for(Building building:allBuildings){
            if(building.buildingId==buildingId){
                return building;
            }
        }
        return null;//building not found
    }
    public static String commandWorker(String commandStringInput){
        String result="";
        String[] commandString=new String[]{commandStringInput};
        String command=getArg(commandString);
        if(command.equals("createBuilding")){
            String buildingName=getArg(commandString);
            String owner=getArg(commandString);
            String plateNumber=getArg(commandString);
            int parkLimit=Integer.parseInt(getArg(commandString));
            String driverName=getArg(commandString);
            addBuilding(buildingName,owner,plateNumber,parkLimit,driverName);
            result=listBuildings();
        }else if(command.equals("createVehicle")){
            int buildingId=Integer.parseInt(getArg(commandString));
            String plateNumber=getArg(commandString);
            String owner=getArg(commandString);
            String driverName=getArg(commandString);
            Building currentBuilding;
            synchronized (Building.class) {
                 currentBuilding = getBuilding(buildingId);
            }
            if (currentBuilding==null)return "Error,Building was not found,";
            Building current=currentBuilding.addVehicle(plateNumber,owner, driverName);
            if(current==null)return "Error,You can not add anymore vehicles,";
            synchronized (Building.class) {
                if (!allBuildings.contains(current)) return "Error,Building was removed, ";
            }
            result=currentBuilding.listVehicles();
        }else if(command.equals("removeVehicle")){
            String buildingId=getArg(commandString);
            String parkId=getArg(commandString);
            Building currentBuilding;
            synchronized (Building.class) {
                currentBuilding=getBuilding(Integer.parseInt(buildingId));
            }
            if(currentBuilding==null)return "Error,Building was not found,";
            currentBuilding.removeVehicle(Integer.parseInt(parkId));
            result = currentBuilding.listVehicles();
        }else if(command.equals("listBuildings")){
            result=listBuildings();
        }else if(command.equals("listVehicles")){
            String buildingId=getArg(commandString);
            Building currentBuilding;
            synchronized (Building.class){
                currentBuilding=getBuilding(Integer.parseInt(buildingId));
            }
            if(currentBuilding==null)return "Error,Building was not found,";
            return currentBuilding.listVehicles();
        }
        return result;
    }
    public static String getArg(String[] commandString){
        //System.out.println(commandString[0]);

        String firstArg=commandString[0].substring(0,commandString[0].indexOf(','));
        if(commandString[0].indexOf(',')+1<commandString[0].length())
            commandString[0]=commandString[0].substring(commandString[0].indexOf(',')+1,commandString[0].length());
        else
            commandString[0]="";
        return firstArg;
    }
}
