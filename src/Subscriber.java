/*
 * Subscriber.java
 *
 * Created on September 26, 2002, 6:00 PM
 */



import java.util.*;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class Subscriber  {

    protected String fromURL;
    protected ServerTransaction serverTransaction;
    protected Request subscribeRequest;
    protected boolean DELAYED;
    private Double Lat;  //¸g«× : 23.697810
    private Double Lng;  //½n«× : 120.960515
    private String subscriberUserName;

    /** Creates new Subscriber */
    public Subscriber(String fromURL,Request subscribeRequest,
        ServerTransaction serverTransaction,boolean DELAYED) {
       this.fromURL=fromURL;
       this.subscribeRequest=subscribeRequest;
       this.serverTransaction=serverTransaction;
       this.DELAYED=DELAYED;
    }
    
    public Request getSubscribeRequest() {
        return subscribeRequest;
    }

    public ServerTransaction getServerTransaction() {
        return serverTransaction;
    }
    
     public String getSubscriberURL() {
        return fromURL;
    }
    
    public boolean isSubscribeDelayed() {
        return DELAYED;
    }
    
    public void setSubscribeDelayed(boolean delay) {
        DELAYED=delay;
    }
}
