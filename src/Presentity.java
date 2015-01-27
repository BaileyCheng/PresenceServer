/*
 * Presentity.java
 *
 * Created on September 30, 2002, 10:00 PM
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
public class Presentity {

    private Hashtable subscriberList;
    private String presentityURL;
    private Double Lat;  //經度 : 23.697810
    private Double Lng;  //緯度 : 120.960515
    private String presentityUserName;
    private PIDF pidf;
    /** Creates new Presentity */
    public Presentity(String username, String presentityURL) {
        subscriberList=new Hashtable();
        pidf = new PIDF(); 
        this.presentityUserName = username;
        this.presentityURL=presentityURL;
    }

    public String getPresentityURL() {
        return presentityURL;
    }
    
    public void setPresentityURL(String url) {
        this.presentityURL= url;
    }
    
    public boolean hasSubscriber(String fromURI) {
        Subscriber subscriber=(Subscriber)subscriberList.get(fromURI);
        if (subscriber==null) return false;
        else {
           return true;
        }
    }
    
    //回傳該presentity的訂閱list中的 指定subscriber
    public Subscriber getSubscriber(String subscriberURI){
    	Subscriber subscriber=(Subscriber)subscriberList.get(subscriberURI);
    	return subscriber;
    }
    public boolean hasSubscriber() {
        return !subscriberList.isEmpty();
    }
    
    public boolean hasSubscriberWaiting() {
        Vector subscribers=getSubscriberList();
        for (int i=0;i<subscribers.size();i++) {
            Subscriber subscriber=(Subscriber) subscribers.elementAt(i);
            if (subscriber.isSubscribeDelayed() ) return true;
        }
        return false;
    }
    
    public Vector getSubscriberList() {
        Collection collection=subscriberList.values();
        return new Vector(collection);
    }

    public void addSubscriber(Subscriber subscrib) {
        subscriberList.put(subscrib.getSubscriberURL(),subscrib);
       
    }
    
    
    //移除subscriber
    public void removeSubscriber(String fromURL) {
        subscriberList.remove(fromURL);
    }
    
    public Double getLat(){
    	return this.Lat;
    }
    
    public Double getLng(){
    	return this.Lng;
    }
    
    public void setLat(Double lat){
    	this.Lat = lat;
    }
    
    public void setLng(Double lng){
    	this.Lng = lng;
    }
    
    //設定該presentity的 username  ex: s1
    public void setUsetName(String username){
    	this.presentityUserName = username;
    }
    
    //取得該presentity的username
    public String getUserName(){
    	return this.presentityUserName;
    }
    
    //設定該presentity的PIDF
    public void setPIDF(String PIDFstring){
    	this.pidf.setPIDF(PIDFstring);
    }
    
    //取得該presentity的PIDF
    public String getPIDF(){
    	return this.pidf.getPIDF();
    }
}
