package ru.rem.server.session.packets.data;

public enum PacketAction{
    
    DOWNLOAD(1, "Скачивание %s завершено"), UPLOAD(2, "Загрузка %s завершена"), UPDATE(3);

    public byte actionId;
    public String name;
    
    private PacketAction(int actionId, String name) {
        this.actionId = format(actionId);
        this.name = name;
    }
    
    private PacketAction(int actionId) {
        this.actionId = format(actionId);
    }
    
    private byte format(int actionId){
        return (byte) (actionId << 4);
    }

    public String getActionName(String addition) {
        return String.format(this.name, addition);
    }
    
    public byte getActionId() {
        return actionId;
    }
    
    public static PacketAction getById(byte id) {
        byte selectId = (byte)(id & 11110000);
        for(PacketAction val: values()){
            if(val.getActionId() == selectId){
                return val;
            }
        }
        return null;
    }
    
}
