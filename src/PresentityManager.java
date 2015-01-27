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
    
    //�P�_�ثe�o�ӭq�\��(fromURL)�O�_�w���q�\present
    public boolean hasSubscriber(String presentityUserName,String fromURL) {
        Presentity presentity=(Presentity)presentityList.get(presentityUserName);  //�qhashtable�̧�Xpresent �ˬd�q�\������T�O���ǤH
        if (presentity==null) return false;
        else {
           return presentity.hasSubscriber(fromURL); // �ˬdfromURL�O�_���q�\present,�^��: ��=true, �_=false
        }
    }
    
    public boolean hasSubscriberWaiting(String present) {
        Presentity presentity=(Presentity)presentityList.get(present);
        if (presentity==null) return false;
        else {
           return presentity.hasSubscriberWaiting();
        }
    }
    
    
    
    //�P�_��presentity�O�_�w���U
    public boolean hasRegister(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	if(presentity != null)return true;
    	else return false;
    }
    
    //�P�_���w��presentity�O�_�٦��q�\��, �S���q�\�̦^��true , 
    public boolean noSusbscriber(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	Vector subscriberList = presentity.getSubscriberList();
    	if(subscriberList.isEmpty())return true;
    	else return false;
    	
    }
    
    //presentity register�� �ppresence server�w���O����presentity �h��sPresentity���
    public void updateRegisteredPresentity(String presentityUserName, String url, Double lat, Double lng){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	presentity.setPresentityURL(url);
    	presentity.setLat(lat);
    	presentity.setLng(lng);
    	System.out.println("PresentityManager, updateRegisteredPresentity(), ��spresentity: "+ presentityUserName+":"+ presentity.getPresentityURL());
    	printPresentityList();
    }
    
    //watcher register�� �ppresence server�w���O����watcher �h��swatcher���
    public void updateRegisteredSubscriber(String subscriberUserName, String subscriberurl, Double lat, Double lng){
    	
    }
    
    //�s�W�@��presentity
    public void addPresentity(String presentityURL, String presentityUserName, Double Lat, Double Lng){
    	Presentity presentity= new Presentity(presentityUserName,presentityURL);
    	//presentity.setUsetName(presentityUserName);
    	presentity.setLat(Lat);
    	presentity.setLng(Lng);
    	presentityList.put(presentityUserName,presentity);
    	System.out.println("PresenceServer �s�W�@��presentity: " + presentityURL);
    	printPresentityList();
    }
    
   //�^�Ǹ�presentity �q�\����subscriber list
    public Vector getSubscriberList(String present) {
        Presentity presentity=(Presentity)presentityList.get(present);
        if (presentity==null) return null;
        else {
           return presentity.getSubscriberList();
        }
    }
    
    //�^�Ǹ�presentity���q�\list���� ���wsubscribe
    public Subscriber getSubscriber(String presentityUserName, String subscriberURI){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	return presentity.getSubscriber(subscriberURI);
    }
    
    
    //��M�g�n�׾F��presentityUserName �æ^��
    public String searchPresentity(Double lat, Double lng){
    	String totalPresentityUserName ="";
        Collection collection=presentityList.values();
        Vector presentities=new Vector(collection);
        
        //�Q��for�j���ˬd�Ҧ�presentity
        for (int i=0;i<presentities.size();i++) {
            Presentity presentity=(Presentity)presentities.elementAt(i);
            //�P�_�U��presentity�g�n�׬O�_�{��watcher
            Double presentityLat = presentity.getLat();
            Double presentityLng = presentity.getLng();
           
            //System.out.println("pLat: "+ pLat +" pLng: "+ pLng +" wLat: "+ wLat + " wLng: "+ wLng);
            
            
            if (presentityLat>= lat-0.003 && presentityLat<= lat+0.003 &&
            		presentityLng>= lng-0.003 && presentityLng <= lng+0.003)	
            		totalPresentityUserName = totalPresentityUserName + presentity.getUserName()+":"+presentity.getLat()+","+presentity.getLng()+";";
             
        }
    	return totalPresentityUserName;
    }
    

    //�s�W�@��Subscriber, �O���� �Υ��ҭq�\��presentity
    public void addSubscriber(String presentityUserName, String presentURL,Subscriber subscriber) {
        Presentity presentity=(Presentity)presentityList.get(presentityUserName); //�P�_�o��presentity�O�_�w�QPresenceServer�Ҫ�
       
        if (presentity==null) {  //�p�GPresenceServer�ثe�S��������presentity�Q�q�\���O�� �h����H�U
           //�إߤ@��presentity, �O���q�\����subscriber, �M��Npresent��presentity��ihashtable��
        	System.out.println("DEBUG, PresentityManager, addSubscriber(), "+
            "We create a new presentity: "+presentityUserName+" and add the subscriber: "+
            subscriber.getSubscriberURL() );
           presentity= new Presentity(presentityUserName,presentURL);
           presentity.addSubscriber(subscriber);
           
           //presentityList.put(present,presentity);
           printPresentityList();
        }
        else { //�p�G�w��presentity�Q�q�\���O�� �h����H�U
            // ��presentity �W�[or��s �q�\����subscriber
        	System.out.println("DEBUG, PresentityManager, addSubscriber(), "+
            "We add or update the subscriber: "+subscriber.getSubscriberURL() +" to "+
            "the presentity: "+presentityUserName);
            presentity.addSubscriber(subscriber);
            printPresentityList();
        }
    }
    
    
    //�qpresentityList���W��presentityUserName��presentity �è��o ��presentity�� URI
    public String getPresentityURI(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	String presentityURI = presentity.getPresentityURL();
    	return presentityURI;
    }
    
    //�̷ӿ�J��presentityUserName ���o�Q�n��presentity
    public Presentity getPresentity(String presentityUserName){
    	Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	return presentity;
    }
    //����presentity
    public void removePresentity(String presentityUserName){
    	 Presentity presentity=(Presentity)presentityList.get(presentityUserName);
    	 presentityList.remove(presentity);
    	 System.out.println("DEBUG, PresentityManager, removePresentity(), ����"+presentityUserName);
    	 printPresentityList();
    }
    
    //�qpresentity �q�\list������ subscriber (fromURL)
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
    
    //���� Subscriber (fromURL) ���Ҧ��q�\��T
    public void removeAllSubscriptions(String fromURL) {
        Collection collection=presentityList.values();
        Vector presentities=new Vector(collection);
        Vector presentitiesBis=new Vector();
        //�Q��for�j���ˬd�Ҧ�presentity
        for (int i=0;i<presentities.size();i++) {
            Presentity presentity=(Presentity)presentities.elementAt(i);
            //�p��presentity���Q��Subscriber�q�\, �h��presentity�N�������q�\list
            if (presentity.hasSubscriber(fromURL) ) {
            	System.out.println("DEBUG: PresentityManager, removeAllSubscriptions(), "+
                   " the subscriber: "+fromURL+" has been removed for the presentity: "+
                   presentity.getPresentityURL());
                    presentity.removeSubscriber(fromURL);
            }
            //�p��presentity�S������q�\��, �h�N��presentity��URL�qpresentityList������
            if ( !presentity.hasSubscriber() ) {
            	System.out.println("DEBUG: PresentityManager, removeSubscriber(), "+
        " the presentity "+presentity.getPresentityURL()+" has been removed (No subscribers!!) ");
                presentityList.remove(presentity.getPresentityURL());
            }
        }
        printPresentityList();
    }
    
    //�L�X�U��Presentity��Subscriber List
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
