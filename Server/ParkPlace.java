package com.company;

public class ParkPlace {
        private String plateNumber;
        private String owner;
        private String driverName;
        private int parkId;
        public ParkPlace(String plateNumber,String owner, String driverName,int parkId){
            this.plateNumber=plateNumber;
            this.owner=owner;
            this.driverName=driverName;
            this.parkId=parkId;
        }
        public int getParkId(){
            return parkId;
        }
        public String getOwner(){
            return owner;
        }
        public String getVehicle(){
            return plateNumber+","+owner+","+driverName+","+parkId+",";
        }
}
