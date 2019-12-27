package com.pos.swoop.objects.XMLMapper;

public class Sector {

    private String from;
    private String to;
    private String type;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if(from == null || from.isEmpty()){
            return "";
        }
        return "from : "+from + " to : "+to;
    }
}