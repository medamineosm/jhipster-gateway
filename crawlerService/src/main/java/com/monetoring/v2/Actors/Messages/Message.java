package com.monetoring.v2.Actors.Messages;

/**
 * Created by Ouasmine on 21/07/2017.
 */
public class Message {

    private String msg;
    private Object object;
    private String reason;

    public Message() {
    }

    public Message(String msg, Object object) {
        this.msg = msg;
        this.object = object;
    }

    public Message(Message message , String msg) {
        this.msg = msg;
        this.object = message.object;
    }

    public Message(Message message , String msg, String reason) {
        this.msg = msg;
        this.object = message.object;
        this.reason = reason;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msg='" + msg + '\'' +
                ", object=" + object +
                '}';
    }
}
