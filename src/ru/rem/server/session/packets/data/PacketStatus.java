package ru.rem.server.session.packets.data;

public enum PacketStatus{
    
    PROCESSED(1), COMPLETE(2), INTERRUPTED(3);
    
    public byte statusId;
    
    private PacketStatus(int statusId) {
        this.statusId = (byte) statusId;
    }

    public byte getStatusId() {
        return statusId;
    }
    
    public static PacketStatus getById(byte id) {
        byte result = (byte) (00001111 & id);
        for(PacketStatus val: values()){
            if(val.getStatusId() == result){
             return val;
            }
        }
        return null;
    }
   
}
