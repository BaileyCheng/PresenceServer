import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

/**
 *
 * @author  deruelle
 * @version 1.0
 */

//�ΨӰO��Server�w�q�\����presentity
public class ServerSubscribeManager {

    private ArrayList presentitySubscirbeList;
    
   
    /** Creates new SubscriberController */
    public ServerSubscribeManager() {
         presentitySubscirbeList=new ArrayList();
    }
    
    
    //�P�_server�O�_�w���q�\presentity
    public boolean hasSubscribed(String presentityUserName){
    	
    	int sub = presentitySubscirbeList.indexOf(presentityUserName);
    	if(sub==-1) return false;
    	else{
    		return true;
    	}
    }
    
    //server �s�W���q�\��presentity �Ψ�pidf
    public void serverAddPresentity(String presentityUserName){
    	//PIDF pidf = new PIDF(PIDF);
    	presentitySubscirbeList.add(presentityUserName);
    	
    	System.out.println("DEBUG, ServerSubscribeManager, serverAddPresentity(): "+ presentityUserName);
    	printPresentityList();   	
    }
    
    //server�������q�\��presentity
    public void serverRemovePresentity(String presentityUserName){
    	presentitySubscirbeList.remove(presentityUserName);
    	System.out.println("DEBUG, ServerSubscribeManager, serverRemovePresentity(): "+ presentityUserName);
    	printPresentityList();
    }
    
  
    /*
    public boolean hasSubscriberWaiting(String presentityUserName) {
        Presentity presentity=(Presentity)presentityList.get(present);
        if (presentity==null) return false;
        else {
           return presentity.hasSubscriberWaiting();
        }
    }
    */

 
   
   
    
    //�L�X�U��Presentity��Subscriber List
    public void printPresentityList() {
        //Collection collection=presentitySubscirbeList.
       // Vector presentities=new Vector(collection);
        System.out.println();
        System.out.println("************* DEBUG ServerSubscribeManager ************************************");
        System.out.println("************* Presentities record:    ************************************");
        System.out.println();
        for (int i=0;i<presentitySubscirbeList.size();i++) {
        	System.out.println(presentitySubscirbeList.get(i));
        }
        System.out.println("**************************************************************************");
        System.out.println();
    }
    
}