/*
 * SubscriberController.java
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
public class PresentityManager {

    private Hashtable presentityList;
   
    /** Creates new SubscriberController */
    public PresentityManager() {
         presentityList=new Hashtable();
    }
    
    //判斷目前這個訂閱者(fromURL)是否已有訂閱present
    public boolean hasSubscriber(String presentityUserName,String fromURL) {
        Presentity presentity=(Presentity)presentityList.get(presentityUserName);  //從hashtable裡找出present 檢查訂閱它的資訊是哪些人
        if (presentity==null) return false;
        else {
           return presentity.hasSubscriber(fromURL); // 檢查fromURL是否有訂閱present,回傳: 有=true, 否=false
        }
    }
    
    public boolean hasSubscriberWaiting(String present) {
        Presentity presentity=(Presentity)presentityList.get(present);
        if (presentity==null) return false;
        else {
           return presentity.hasSubscriberWaiting();
        }
    }
    
    
    
    //判斷該presentity是否已註冊
    public boolean hasRegister(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	if(presentity != null)return true;
    	else return false;
    }
    
    //判斷指定的presentity是否還有訂閱者, 沒有訂閱者回傳true , 
    public boolean noSusbscriber(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	Vector subscriberList = presentity.getSubscriberList();
    	if(subscriberList.isEmpty())return true;
    	else return false;
    	
    }
    
    //presentity register時 如presence server已有記錄此presentity 則更新Presentity資料
    public void updateRegisteredPresentity(String presentityUserName, String url, Double lat, Double lng){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	presentity.setPresentityURL(url);
    	presentity.setLat(lat);
    	presentity.setLng(lng);
    	System.out.println("PresentityManager, updateRegisteredPresentity(), 更新presentity: "+ presentityUserName+":"+ presentity.getPresentityURL());
    	printPresentityList();
    }
    
    //watcher register時 如presence server已有記錄此watcher 則更新watcher資料
    public void updateRegisteredSubscriber(String subscriberUserName, String subscriberurl, Double lat, Double lng){
    	
    }
    
    //新增一個presentity
    public void addPresentity(String presentityURL, String presentityUserName, Double Lat, Double Lng){
    	Presentity presentity= new Presentity(presentityUserName,presentityURL);
    	//presentity.setUsetName(presentityUserName);
    	presentity.setLat(Lat);
    	presentity.setLng(Lng);
    	presentityList.put(presentityUserName,presentity);
    	System.out.println("PresenceServer 新增一個presentity: " + presentityURL);
    	printPresentityList();
    }
    
   //回傳該presentity 訂閱它的subscriber list
    public Vector getSubscriberList(String present) {
        Presentity presentity=(Presentity)presentityList.get(present);
        if (presentity==null) return null;
        else {
           return presentity.getSubscriberList();
        }
    }
    
    //回傳該presentity的訂閱list中的 指定subscribe
    public Subscriber getSubscriber(String presentityUserName, String subscriberURI){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	return presentity.getSubscriber(subscriberURI);
    }
    
    
    //找尋經緯度鄰近的presentityUserName 並回傳
    public String searchPresentity(Double lat, Double lng){
    	String totalPresentityUserName ="";
        Collection collection=presentityList.values();
        Vector presentities=new Vector(collection);
        
        //利用for迴圈檢查所有presentity
        for (int i=0;i<presentities.size();i++) {
            Presentity presentity=(Presentity)presentities.elementAt(i);
            //判斷各個presentity經緯度是否臨近watcher
            Double presentityLat = presentity.getLat();
            Double presentityLng = presentity.getLng();
           
            //System.out.println("pLat: "+ pLat +" pLng: "+ pLng +" wLat: "+ wLat + " wLng: "+ wLng);
            
            
            if (presentityLat>= lat-0.003 && presentityLat<= lat+0.003 &&
            		presentityLng>= lng-0.003 && presentityLng <= lng+0.003)	
            		totalPresentityUserName = totalPresentityUserName + presentity.getUserName()+":"+presentity.getLat()+","+presentity.getLng()+";";
             
        }
    	return totalPresentityUserName;
    }
    

    //新增一個Subscriber, 記錄它 及它所訂閱的presentity
    public void addSubscriber(String presentityUserName, String presentURL,Subscriber subscriber) {
        Presentity presentity=(Presentity)presentityList.get(presentityUserName); //判斷這個presentity是否已被PresenceServer所知
       
        if (presentity==null) {  //如果PresenceServer目前沒有任何有關presentity被訂閱的記錄 則執行以下
           //建立一個presentity, 記錄訂閱它的subscriber, 然後將present及presentity放進hashtable裡
        	System.out.println("DEBUG, PresentityManager, addSubscriber(), "+
            "We create a new presentity: "+presentityUserName+" and add the subscriber: "+
            subscriber.getSubscriberURL() );
           presentity= new Presentity(presentityUserName,presentURL);
           presentity.addSubscriber(subscriber);
           
           //presentityList.put(present,presentity);
           printPresentityList();
        }
        else { //如果已有presentity被訂閱的記錄 則執行以下
            // 幫presentity 增加or更新 訂閱它的subscriber
        	System.out.println("DEBUG, PresentityManager, addSubscriber(), "+
            "We add or update the subscriber: "+subscriber.getSubscriberURL() +" to "+
            "the presentity: "+presentityUserName);
            presentity.addSubscriber(subscriber);
            printPresentityList();
        }
    }
    
    
    //從presentityList找到名為presentityUserName的presentity 並取得 該presentity的 URI
    public String getPresentityURI(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	String presentityURI = presentity.getPresentityURL();
    	return presentityURI;
    }
    
    //依照輸入的presentityUserName 取得想要的presentity
    public Presentity getPresentity(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	return presentity;
    }
    //移除presentity
    public void removePresentity(String presentityUserName){
    	 Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	 presentityList.remove(presentity);
    	 System.out.println("DEBUG, PresentityManager, removePresentity(), 移除"+presentityUserName);
    	 printPresentityList();
    }
    
    //從presentity 訂閱list中移除 subscriber (fromURL)
    public void removeSubscriber(String present,String fromURL) {
        Presentity presentity=(Presentity)presentityList.get(present);
        if (presentity!=null) {
        	System.out.println("DEBUG: PresentityManager, removeSubscriber(), "+
        " the subscriber: "+fromURL+" has been removed for the presentity: "+
             present);
            presentity.removeSubscriber(fromURL);
            // If the presentity does not have any subscribers, we remove it
            // also:
            /*
            if ( !presentity.hasSubscriber() ) {
            	System.out.println("DEBUG: PresentityManager, removeSubscriber(), "+
        " the presentity "+present+" has been removed (No subscribers!!) ");
                presentityList.remove(present);
            }
            */
        }
        else System.out.println("DEBUG: PresentityManager, removeSubscriber(), "+
        " the presentity: "+present+" was not found... The subscriber: "+fromURL+" is not removed! ");
        printPresentityList();
    }
    
    //移除 Subscriber (fromURL) 的所有訂閱資訊
    public void removeAllSubscriptions(String fromURL) {
        Collection collection=presentityList.values();
        Vector presentities=new Vector(collection);
        Vector presentitiesBis=new Vector();
        //利用for迴圈檢查所有presentity
        for (int i=0;i<presentities.size();i++) {
            Presentity presentity=(Presentity)presentities.elementAt(i);
            //如該presentity有被該Subscriber訂閱, 則該presentity將之移除訂閱list
            if (presentity.hasSubscriber(fromURL) ) {
            	System.out.println("DEBUG: PresentityManager, removeAllSubscriptions(), "+
                   " the subscriber: "+fromURL+" has been removed for the presentity: "+
                   presentity.getPresentityURL());
                    presentity.removeSubscriber(fromURL);
            }
            //如該presentity沒有任何訂閱者, 則將該presentity的URL從presentityList中移除
            if ( !presentity.hasSubscriber() ) {
            	System.out.println("DEBUG: PresentityManager, removeSubscriber(), "+
        " the presentity "+presentity.getPresentityURL()+" has been removed (No subscribers!!) ");
                presentityList.remove(presentity.getPresentityURL());
            }
        }
        printPresentityList();
    }
    
    //印出各個Presentity的Subscriber List
    public void printPresentityList() {
        Collection collection=presentityList.values();
        Vector presentities=new Vector(collection);
        System.out.println();
        System.out.println("************* DEBUG PresentityManager ************************************");
        System.out.println("************* Presentities record:    ************************************");
        System.out.println();
        for (int i=0;i<presentities.size();i++) {
            Presentity presentity=(Presentity)presentities.elementAt(i);
            System.out.println("presentity: "+presentity.getUserName());
            System.out.println("presentity: "+presentity.getPresentityURL());
            Vector subscribers=presentity.getSubscriberList();
            for (int j=0;j<subscribers.size();j++) {
                Subscriber subscriber=(Subscriber)subscribers.elementAt(j);
                System.out.println("    subscriber: "+subscriber.getSubscriberURL()+
                "   serverTransaction:"+ subscriber.getServerTransaction()+"   delay:"+subscriber.isSubscribeDelayed());
            }
            System.out.println();
        }
        System.out.println("**************************************************************************");
        System.out.println();
    }
    
}
