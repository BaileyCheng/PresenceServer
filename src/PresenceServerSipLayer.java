

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class PresenceServerSipLayer implements SipListener {

    private MessageProcessor messageProcessor;

    private String username;

    private SipStack sipStack;

    private SipFactory sipFactory;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;
    
    Dialog dialog;
    
    Vector vector = new Vector();
    
    PresentityManager presentityManager;

    ServerSubscribeManager serverSubscribeManager;
    
    

    /** Here we initialize the SIP stack. */
    public PresenceServerSipLayer(String username, String ip, int port)
	    throws PeerUnavailableException, TransportNotSupportedException,
	    InvalidArgumentException, ObjectInUseException,
	    TooManyListenersException {
    	
    presentityManager=new PresentityManager();
    serverSubscribeManager = new ServerSubscribeManager(); 	
	setUsername(username);
	sipFactory = SipFactory.getInstance();
	sipFactory.setPathName("gov.nist");
	Properties properties = new Properties();
	properties.setProperty("javax.sip.STACK_NAME", "PresenceServer");
	properties.setProperty("javax.sip.IP_ADDRESS", ip);

	//DEBUGGING: Information will go to files 
	//textclient.log and textclientdebug.log
	properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
	properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
		"PresenceServer.txt");
	properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
		"PresenceServerdebug.log");

	sipStack = sipFactory.createSipStack(properties);
	headerFactory = sipFactory.createHeaderFactory();
	addressFactory = sipFactory.createAddressFactory();
	messageFactory = sipFactory.createMessageFactory();

	ListeningPoint tcp = sipStack.createListeningPoint(port, "tcp");
	ListeningPoint udp = sipStack.createListeningPoint(port, "udp");

	sipProvider = sipStack.createSipProvider(tcp);
	sipProvider.addSipListener(this);
	sipProvider = sipStack.createSipProvider(udp);
	sipProvider.addSipListener(this);
    }

    /**
     * This method uses the SIP stack to send a message. 
     */
    public void sendMessage(String to, String message) throws ParseException,
	    InvalidArgumentException, SipException {

    	SipURI from = addressFactory.createSipURI(getUsername(), getHost()
    			+ ":" + getPort());
    		Address fromNameAddress = addressFactory.createAddress(from);
    		fromNameAddress.setDisplayName(getUsername());
    		FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
    			"PresenceServer");

    		String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
    		String address = to.substring(to.indexOf("@") + 1);

    		SipURI toAddress = addressFactory.createSipURI(username, address);
    		Address toNameAddress = addressFactory.createAddress(toAddress);
    		toNameAddress.setDisplayName(username);
    		ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

    		SipURI requestURI = addressFactory.createSipURI(username, address);
    		requestURI.setTransportParam("udp");

    		ArrayList viaHeaders = new ArrayList();
    		ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
    			getPort(), "udp", "branch1");
    		viaHeaders.add(viaHeader);

    		CallIdHeader callIdHeader = sipProvider.getNewCallId();

    		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1,
    			Request.MESSAGE);

    		MaxForwardsHeader maxForwards = headerFactory
    			.createMaxForwardsHeader(70);

    		Request request = messageFactory.createRequest(requestURI,
    			Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
    			toHeader, viaHeaders, maxForwards);

    		SipURI contactURI = addressFactory.createSipURI(getUsername(),
    			getHost());
    		contactURI.setPort(getPort());
    		Address contactAddress = addressFactory.createAddress(contactURI);
    		contactAddress.setDisplayName(getUsername());
    		ContactHeader contactHeader = headerFactory
    			.createContactHeader(contactAddress);
    		request.addHeader(contactHeader);


    		ContentTypeHeader contentTypeHeader = headerFactory
    			.createContentTypeHeader("text", "plain");
    		request.setContent(message, contentTypeHeader);

    		sipProvider.sendRequest(request);


    	
    }
    
    public void sendSubscribe(String to, String message, Boolean unsubscribe) throws ParseException,
    InvalidArgumentException, SipException { 
    	SipURI from = addressFactory.createSipURI(getUsername(), getHost()
    			+ ":" + getPort());
    		Address fromNameAddress = addressFactory.createAddress(from);
    		fromNameAddress.setDisplayName(getUsername());
    		FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
    			"PresenceServer");

    		String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
    		String address = to.substring(to.indexOf("@") + 1);

    		SipURI toAddress = addressFactory.createSipURI(username, address);
    		Address toNameAddress = addressFactory.createAddress(toAddress);
    		toNameAddress.setDisplayName(username);
    		ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

    		SipURI requestURI = addressFactory.createSipURI(username, address);
    		requestURI.setTransportParam("udp");

    		ArrayList viaHeaders = new ArrayList();
    		ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
    			getPort(), "udp", "branch1");
    		viaHeaders.add(viaHeader);

    		CallIdHeader callIdHeader = sipProvider.getNewCallId();

    		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1,
    			Request.SUBSCRIBE);

    		MaxForwardsHeader maxForwards = headerFactory
    			.createMaxForwardsHeader(70);

    		Request request = messageFactory.createRequest(requestURI,
    			Request.SUBSCRIBE, callIdHeader, cSeqHeader, fromHeader,
    			toHeader, viaHeaders, maxForwards);

    		SipURI contactURI = addressFactory.createSipURI(getUsername(),
    			getHost());
    		contactURI.setPort(getPort());
    		Address contactAddress = addressFactory.createAddress(contactURI);
    		contactAddress.setDisplayName(getUsername());
    		ContactHeader contactHeader = headerFactory
    			.createContactHeader(contactAddress);
    		request.addHeader(contactHeader);
    		EventHeader eventHeader = headerFactory.createEventHeader("state");//--SUB
    		request.addHeader(eventHeader);//---SUB

    		ExpiresHeader expiresHeader = null;//-----------------SUB������
    		if(unsubscribe== true){
    			expiresHeader = headerFactory.createExpiresHeader(0);//----UNSUB
    		}
    		else{
    			expiresHeader = headerFactory.createExpiresHeader(600000);//----SUB������
    		}
    		
    		request.addHeader(expiresHeader);//-----------------SUB������

    		ContentTypeHeader contentTypeHeader = headerFactory
    			.createContentTypeHeader("text", "plain");
    		request.setContent(message, contentTypeHeader);

    		sipProvider.sendRequest(request);


    }

 
    
    /** This method is called by the SIP stack when a response arrives. */
    public void processResponse(ResponseEvent evt) {
    	
    System.out.println("Got a response");
	Response response = (Response) evt.getResponse();
	Transaction tid = evt.getClientTransaction();

	System.out.println(
		"Response received with client transaction id "
			+ tid
			+ ":\n"
			+ response);
	if (tid == null) {
		System.out.println("Stray response -- dropping ");
		return;
	}
	System.out.println("transaction state is " + tid.getState());
	System.out.println("Dialog = " + tid.getDialog());
	System.out.println("Dialog State is " + tid.getDialog().getState());
	
	int status = response.getStatusCode();

	if ((status >= 200) && (status < 300)) { //Success!
	    messageProcessor.processInfo("--Sent");
	    return;
	}

	messageProcessor.processError("Previous message not sent: status " + status);
    }

    /** 
     * This method is called by the SIP stack when a new request arrives. 
     */
    public void processRequest(RequestEvent evt) {
	//Request req = evt.getRequest();
	
	//String method = req.getMethod();
	/*
	if (!method.equals("MESSAGE")) { //bad request type.
	    messageProcessor.processError("Bad request type: " + method);
	    return;
	}
	*/
	
	Request request = evt.getRequest();
	ServerTransaction serverTransactionId = evt
			.getServerTransaction();
	String method = request.getMethod();
	
	System.out.println("\n\nRequest " + request.getMethod()
			+ " received at " + sipStack.getStackName()
			+ " with server transaction id " + serverTransactionId);
	FromHeader from = (FromHeader) request.getHeader("From");
	
	Response response = null;

	if (method.equals("SUBSCRIBE")) {
		messageProcessor.processMessage(from.getAddress().toString(),
				new String(request.getRawContent()));
		/*
		System.out.println("got a subscire");


		 try { //Reply with OK
			 response = messageFactory.createResponse(200, request);
			 ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			 toHeader.setTag("888"); //This is mandatory as per the spec.
			 ServerTransaction st = sipProvider.getNewServerTransaction(request);
	    	ExpiresHeader expiresHeader = null;//-----------------SUB������
	    	expiresHeader = headerFactory.createExpiresHeader(600000);//----SUB������
	    	response.addHeader(expiresHeader);//-----------------SUB������
	    
	    	st.sendResponse(response);
		} catch (Throwable e) {
			e.printStackTrace();
			messageProcessor.processError("Can't send OK reply.");
		}
	 */
		processSubscribe(evt, serverTransactionId);
	} 
	else if (request.getMethod().equals(Request.PUBLISH)){
		
		try { //Reply with OK
		    response = messageFactory.createResponse(200, request);
		    ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
		    toHeader.setTag("888"); //This is mandatory as per the spec.
		    ServerTransaction st = sipProvider.getNewServerTransaction(request);
		    st.sendResponse(response);
		   // processNotify(new String(request.getRawContent()));
		    //System.out.println();
		} catch (Throwable e) {
		    e.printStackTrace();
		    messageProcessor.processError("Can't send OK reply.");
		}

	}
	else if (request.getMethod().equals(Request.REGISTER)){
		try { //Reply with OK

		    response = messageFactory.createResponse(Response.OK, request);
		    ServerTransaction st = sipProvider.getNewServerTransaction(request);
		    st.sendResponse(response);
		    
		    processRegisterRequest(sipProvider,request);
		    

		} catch (Throwable e) {
		    e.printStackTrace();
		    messageProcessor.processError("Can't send OK reply.");
		}
	}else if(method.equals("NOTIFY")){
		messageProcessor.processMessage(from.getAddress().toString(),
				new String(request.getRawContent()));
		//processNotify(evt,request, serverTransactionId);
		
		try { //Reply with OK

   			System.out.println("PresenceServer:  got a notify from "+ from.getAddress().toString());
			if (serverTransactionId == null) {
				System.out.println("subscriber:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			response = messageFactory.createResponse(200,request);
			serverTransactionId.sendResponse(response);
			SubscriptionStateHeader subscriptionState =  (SubscriptionStateHeader) 
						request.getHeader(SubscriptionStateHeader.NAME);
			// Subscription is terminated.
			if ( subscriptionState.getState().equals(SubscriptionStateHeader.TERMINATED)) {
				dialog.delete();
				//notify all subscriber Subscription is terminated. ������presentity�Ҧ���T
				processNotifyAll(getUsername(getKey(request,"From")), new String(request.getRawContent()),true);
				presentityManager.removePresentity(getUsername(getKey(request,"From")));
				serverSubscribeManager.serverRemovePresentity(getUsername(getKey(request,"From")));
				return;
			}
			processNotifyAll(getUsername(getKey(request,"From")), new String(request.getRawContent()),false);

		} catch (Throwable e) {
		    e.printStackTrace();
		    messageProcessor.processError("Request.NOTIFY Can't send OK reply.");
		}
		// ����notify �Anotify all subscriber, process notify
	
		
	
	
	}
	
	
	/*
	 if (method.equals("NOTIFY"))//----------send 200 OK
		{
			if(((EventHeader)req.getHeader(EventHeader.NAME))
					 .getEventType().equalsIgnoreCase("state"))
			{
				
				ServerTransaction serverTransaction = null;
				  serverTransaction = evt.getServerTransaction();
				  
				  try
				    {
				    	serverTransaction = sipProvider.getNewServerTransaction(req);
				    }
				    catch(TransactionUnavailableException e)
				    {
				    	e.printStackTrace();
				    }
				    catch(TransactionAlreadyExistsException e)
				    {
				    	e.printStackTrace();
				    }
				
				FromHeader from = (FromHeader) req.getHeader("From");
				messageProcessor.processMessage(from.getAddress().toString(),
					new String(req.getRawContent()));
				Response response = null;
				//SubscriptionStateHeader  subscriptionStateHeader= null;
				try { //Reply with OK
				    response = messageFactory.createResponse(200, req);
				    //ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				    //toHeader.setTag("888"); //This is mandatory as per the spec.
				    //ServerTransaction st = sipProvider.getNewServerTransaction(req);
				    //subscriptionStateHeader=headerFactory.createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
				    //response.addHeader(subscriptionStateHeader);
				    
				    //st.sendResponse(response);
				    
					serverTransaction.sendResponse(response);
				    //sipProvider.sendResponse(response);
				} catch (Throwable e) {
				    e.printStackTrace();
				    messageProcessor.processError("Can't send OK reply.");
					}
			}
    	
		}
	 else if (method.equals("SUBSCRIBE")){
		 FromHeader from = (FromHeader) req.getHeader("From");
		 messageProcessor.processMessage(from.getAddress().toString(),
				 new String(req.getRawContent()));
		 Response response = null;
		 try { //Reply with OK
			 response = messageFactory.createResponse(200, req);
			 ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			 toHeader.setTag("888"); //This is mandatory as per the spec.
			 ServerTransaction st = sipProvider.getNewServerTransaction(req);
	    	ExpiresHeader expiresHeader = null;//-----------------SUB������
	    	expiresHeader = headerFactory.createExpiresHeader(600000);//----SUB������
	    	response.addHeader(expiresHeader);//-----------------SUB������
	    
	    	st.sendResponse(response);
		} catch (Throwable e) {
			e.printStackTrace();
			messageProcessor.processError("Can't send OK reply.");
		}
	 }
	 */
    }

    
    public void processSubscribe(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
    	//vector.add((requestEvent);
    	//vector.addElement(serverTransaction);
    	
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		
		String fromURL=getKey(request,"From");
		System.out.println(fromURL);  // �L�X sip:s1@192.168.188.75:5061
		String ToURL=getKey(request,"To");
		String ToUserName= getUsername(ToURL);
		System.out.println(ToUserName);
		ExpiresHeader expiresHeader=(ExpiresHeader)request.getHeader(ExpiresHeader.NAME);
		ServerTransaction st = requestEvent.getServerTransaction();
		//����Subscribe, ��response OK
		try {
			System.out.println("PresenceServer: got an Subscribe sending OK");
			System.out.println("PresenceServer:  " + request);
			EventHeader eventHeader = (EventHeader) request
					.getHeader(EventHeader.NAME);
			if (eventHeader == null) {
				System.out
						.println("Cannot find event header.... dropping request.");
				return;
			}
			
			Response response = messageFactory.createResponse(202, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			Address address = addressFactory
					.createAddress(":PresenceServer");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			ExpiresHeader expiresHeader1 = null;//-----------------SUB������
			if (expiresHeader!=null && expiresHeader.getExpires()==0 ){
				expiresHeader1 = headerFactory.createExpiresHeader(0);
			}else{
			expiresHeader1 = headerFactory.createExpiresHeader(600000);//----SUB������
			}
			response.addHeader(expiresHeader);//-----------------SUB������

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			System.out.println("got a server transaction " + st);

			this.dialog = st.getDialog();
			// subscribe dialogs do not terminate on bye.
			this.dialog.terminateOnBye(false);
			if (dialog != null) {
				System.out.println("Dialog " + dialog);
				System.out.println("Dialog state " + dialog.getState());
			}
			st.sendResponse(response);
			if (dialog != null) {
				System.out.println("Dialog " + dialog);
				System.out.println("Dialog state " + dialog.getState());
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String toPresentityURI = presentityManager.getPresentityURI(getUsername(ToURL));
		String message="subscribe";
		
		//���P�_server�O�_�w�q�\��presentity, 
		if(serverSubscribeManager.hasSubscribed(ToUserName)){
			//server�w���q�\��presentity �h��X��presentity��PIDF
			Presentity presentity = presentityManager.getPresentity(ToUserName);
	    	String pidf = presentity.getPIDF();
			//Server�w�q�\, watcher�]�w�q�\presentity
			if(presentityManager.hasSubscriber(ToUserName, fromURL)){
				
				if (expiresHeader!=null && expiresHeader.getExpires()==0 ){
					//watcher unsubscribe 
					//�qpresentity���q�\list������ subscriber (fromURL)
					processNotify(ToUserName,fromURL);
					presentityManager.removeSubscriber(ToUserName, fromURL);
					//�P�_��presentity�O�_�٦��q�\��  �p�G�S���q�\�� �hserver�o�Xunsubsribe��presentity
					if(presentityManager.noSusbscriber(ToUserName)){
						try{
							System.out.println("DEBUG, PresenceServer, presentityManager.noSusbscriber()");
							
						sendSubscribe(presentity.getPresentityURL(), message, true);
						} catch (Exception ex) {
							System.out.println("DEBUG, PresenceSever, can't send unSubscribe()"  );
							ex.printStackTrace();
							
						}
					}
				}else{
					//update watcher, notify presentity's pidf
					addSubscriber(request,st,false);
					processNotify(ToUserName,fromURL);
				}
				
			}else{ //server���q�\, watcher���q�\, addsubscriber, ��notify!!!
				//presentityManager.addSubscriber(ToUserName, ToURL, subscriber)
				addSubscriber(request,st,false);
				processNotify(ToUserName,fromURL);
			}
		}else{ // server�٥��q�\, server���h�q�\presentity ����notify �Anotify all subscriber
			//server subscribe presentity, subscribe success and then to do notifyallsubscriber
			//�s�Wsubscriber �ðO�������bwaiting notify
			addSubscriber(request,st,true);
			
			/*�ھڭq�\��presentityUserName  �qserverSubscribeManager��X��presentity��ڪ�URI 
			 *�M��o�esubscirbe�T������presentity
			**/
			
			try{
			//�o�esubscribe�T��
			sendSubscribe(toPresentityURI, message, false);
			
			//server�O�����q�\�F��presentity
			serverSubscribeManager.serverAddPresentity(ToUserName);
			} catch (Exception ex) {
				System.out.println("DEBUG, PresenceSever, can't do sendSubscribe()"  );
				ex.printStackTrace();
				
			}
			
			
		}
		
		/*
		try {
			System.out.println("PresenceServer: got an Subscribe sending OK");
			System.out.println("PresenceServer:  " + request);
			EventHeader eventHeader = (EventHeader) request
					.getHeader(EventHeader.NAME);
			if (eventHeader == null) {
				System.out
						.println("Cannot find event header.... dropping request.");
				return;
			}
			Response response = messageFactory.createResponse(202, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			//Address address = addressFactory
			//		.createAddress("PresenceServer <sip:s1@192.168.188.75:5061>");
			Address address = addressFactory.createAddress("PresenceServer");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			
			ExpiresHeader expiresHeader = null;//-----------------SUB������
			expiresHeader = headerFactory.createExpiresHeader(600000);//----SUB������
			response.addHeader(expiresHeader);//-----------------SUB������
			
			
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			System.out.println("got a server transaction " + st);

			this.dialog = st.getDialog();
			// subscribe dialogs do not terminate on bye.
			this.dialog.terminateOnBye(false);
			if (dialog != null) {
				System.out.println("Dialog " + dialog);
				System.out.println("Dialog state " + dialog.getState());
			}
			st.sendResponse(response);
			if (dialog != null) {
				System.out.println("Dialog " + dialog);
				System.out.println("Dialog state " + dialog.getState());
			}
			/*
			 * NOTIFY requests MUST contain a "Subscription-State" header with a
			 * value of "active", "pending", or "terminated". The "active" value
			 * indicates that the subscription has been accepted and has been
			 * authorized (in most cases; see section 5.2.). The "pending" value
			 * indicates that the subscription has been received, but that
			 * policy information is insufficient to accept or deny the
			 * subscription at this time. The "terminated" value indicates that
			 * the subscription is not active.
			 *
			Request notifyRequest = dialog.createRequest(Request.NOTIFY);
			SubscriptionStateHeader sstate = headerFactory
					.createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
			notifyRequest.addHeader(sstate);
			notifyRequest.addHeader(eventHeader);
			ClientTransaction ct = sipProvider
					.getNewClientTransaction(notifyRequest);
			dialog.sendRequest(ct);
			vector.add(dialog);
			System.out.println(vector.size());
			System.out.println(vector.get(0));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		*/
	}
    
    
   


    public void processNotifyAll(String presentityUserName,String PIDF, Boolean terminated){

    	//�]�w��Presentity�� PIDF
    	Presentity presentity = presentityManager.getPresentity(presentityUserName);
    	presentity.setPIDF(PIDF);
    	Vector subscriberList = presentityManager.getSubscriberList(presentityUserName);

        for (int i=0;i<subscriberList.size();i++) {
            Subscriber subscriber=(Subscriber) subscriberList.elementAt(i);
            ServerTransaction st = subscriber.getServerTransaction();
            if(st==null)
            	System.out.println("st==null");
        	Dialog dialog = st.getDialog();
        	
            try {
            	
            	Request notifyRequest = dialog.createRequest(Request.NOTIFY);
            	
            	//��dialog���A��ACTIVE���ܦ�TERMINATED
            	SubscriptionStateHeader subscriptionStateHeader= (SubscriptionStateHeader)notifyRequest.getHeader("SubscriptionState");
            	if(terminated){
            		SubscriptionStateHeader sstate = headerFactory
					.createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
    			notifyRequest.addHeader(sstate);
            	}else{
            		SubscriptionStateHeader sstate = headerFactory
        			.createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
    			notifyRequest.addHeader(sstate);
            	}
           	
     
        		EventHeader eventHeader = headerFactory.createEventHeader("presence");
        		notifyRequest.addHeader(eventHeader);
        		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
        		notifyRequest.setContent(PIDF, contentTypeHeader);
        		ClientTransaction ct = sipProvider
        				.getNewClientTransaction(notifyRequest);
        		dialog.sendRequest(ct);
            	} catch (Throwable e) {
        		    e.printStackTrace();
        		    messageProcessor.processError("Can't send Notify.");
        		}
            	//��s��subscriber �O�����
            	addSubscriber(subscriber.getSubscribeRequest(),st,false);
                //sendSubscribeRequest(sipProvider, 
	//subscribeRequest,subscriber.serverTransaction);
           
        }
    	
    }
    
    public void processNotify(String presentityUserName,String subscriberURI){

    	//��X�q�\��presentity�� subscriber �Υ���transaction
    	Presentity presentity = presentityManager.getPresentity(presentityUserName);
    	String pidf = presentity.getPIDF();
    	Subscriber subscriber = presentityManager.getSubscriber(presentityUserName, subscriberURI);
        ServerTransaction st = subscriber.getServerTransaction();
            if(st==null)
            	System.out.println("st==null");
        		Dialog dialog = st.getDialog();
        	
            try {
            	
            	Request notifyRequest = dialog.createRequest(Request.NOTIFY);
        		SubscriptionStateHeader sstate = headerFactory
        				.createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
        		notifyRequest.addHeader(sstate);
        		EventHeader eventHeader = headerFactory.createEventHeader("presence");
        		notifyRequest.addHeader(eventHeader);
        		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
        		notifyRequest.setContent(pidf, contentTypeHeader);
        		ClientTransaction ct = sipProvider
        				.getNewClientTransaction(notifyRequest);
        		dialog.sendRequest(ct);
            	} catch (Throwable e) {
        		    e.printStackTrace();
        		    messageProcessor.processError("Can't send Notify.");
        		}
            	//��s��subscriber �O�����
            	addSubscriber(subscriber.getSubscribeRequest(),st,false);
                //sendSubscribeRequest(sipProvider, 
	//subscribeRequest,subscriber.serverTransaction);
           
        
    	
    }
    
    /*
    public void processNotify(String PIDF){
    	for(int i=0;i<vector.size();i++){
            //RequestEvent requestEvent = (RequestEvent)vector.get(i);
            //ServerTransaction st = requestEvent.getServerTransaction();
        	this.dialog = (Dialog)vector.get(i);
            //this.dialog = st.getDialog();
        	try {
        	Request notifyRequest = dialog.createRequest(Request.NOTIFY);
    		SubscriptionStateHeader sstate = headerFactory
    				.createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
    		notifyRequest.addHeader(sstate);
    		EventHeader eventHeader = headerFactory.createEventHeader("presence");
    		notifyRequest.addHeader(eventHeader);
    		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
    		notifyRequest.setContent(PIDF, contentTypeHeader);
    		ClientTransaction ct = sipProvider
    				.getNewClientTransaction(notifyRequest);
    		dialog.sendRequest(ct);
        	} catch (Throwable e) {
    		    e.printStackTrace();
    		    messageProcessor.processError("Can't send Notify.");
    		}
    	}
    	
    }
     */    

    public static URI getCleanUri(URI uri) {
        if (uri instanceof SipURI) {
            SipURI sipURI=(SipURI)uri.clone();
            
            Iterator iterator=sipURI.getParameterNames();
            while (iterator!=null && iterator.hasNext()) {
                String name=(String)iterator.next();
                sipURI.removeParameter(name);
            }
            return  sipURI;
        }
        else return  uri;
    }
    
    
  //�ǤJrequest �έn�䪺header (From or To), �^�Ǹ�header��address
    public  String getKey(Message message,String header) {
        try{
            Address address=null;
            if (header.equals("From") ) {
                FromHeader fromHeader=(FromHeader)message.getHeader(FromHeader.NAME);
                address=fromHeader.getAddress();
                
            }
            else
                if (header.equals("To") ) {
                    ToHeader toHeader=(ToHeader)message.getHeader(ToHeader.NAME);
                    address=toHeader.getAddress();
                    
                }
                
            javax.sip.address.URI  cleanedUri=null;
            if (address==null) {
                cleanedUri= getCleanUri( ((Request)message).getRequestURI());
            }
            else {
                // We have to build the key, all
                // URI parameters MUST be removed:
                cleanedUri = getCleanUri(address.getURI());
            }
            
            if (cleanedUri==null) return null;
            
            String  keyresult=cleanedUri.toString();
            System.out.println("DEBUG, PresenceServer, getKey(), the key is: " + 
            keyresult);
            return keyresult.toLowerCase();
            
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getUsername(String uri) {
    	String username = uri.substring(uri.indexOf(":") + 1,uri.indexOf("@"));	
            return  username;
        }
    
    public static String getUserport(String uri){
    	String preuserport = uri.substring(uri.indexOf("@"));
    	String userport = preuserport.substring(preuserport.indexOf(":"));
    	return userport;
    }

    
    public boolean hasSubscriberWaiting(Request registerRequest) {
        String fromURL=getKey(registerRequest,"From");
        return presentityManager.hasSubscriberWaiting(fromURL);
    }
    
    
    //���o ��presentity  �q�\�����q�\��list
    public Vector getSubscriberList(Request registerRequest) {
        String presentity=getKey(registerRequest,"From");
        return presentityManager.getSubscriberList(presentity);
    }
    
    //�qpresentity��list������ ��subscriber(fromURL)
    public void removeSubscriber(Request subscribeRequest) {
        String presentity=getKey(subscribeRequest,"To");
        String fromURL=getKey(subscribeRequest,"From");
        presentityManager.removeSubscriber(presentity,fromURL);
    }
    
    //���� Subscriber (fromURL) ���Ҧ��q�\��T
    public void removeAllSubscriptions(String fromURL) {
        presentityManager.removeAllSubscriptions(fromURL);
    }
    
    public void removePresentity(String presentityUserName){
    	presentityManager.removePresentity(presentityUserName);
    	serverSubscribeManager.serverRemovePresentity(presentityUserName);
    }
    
    
    public void processRegisterRequest(SipProvider sipProvider,
    		Request registerRequest) {
		String LatandLng = new String(registerRequest.getRawContent());
		String type = LatandLng.substring(0,LatandLng.indexOf(":"));
		String Lat = LatandLng.substring(LatandLng.indexOf(":")+1, LatandLng.indexOf(","));
		String Lng = LatandLng.substring(LatandLng.indexOf(",") + 1);
		System.out.println(type);
		System.out.println(Double.parseDouble(Lat));
		System.out.println(Double.parseDouble(Lng));
		
		
		String fromURL=getKey(registerRequest,"From");
		System.out.println(fromURL);  // �L�X sip:s1@192.168.188.75:5061
		//System.out.println(request.getRequestURI()); sip:s5@192.168.188.75:5065;transport=udp
		String sipUserName = getUsername(fromURL);
		System.out.println(sipUserName);  //�L�X s1
		String ToURL=getKey(registerRequest,"To");
		
		ExpiresHeader expiresHeader=(ExpiresHeader)registerRequest.getHeader(ExpiresHeader.NAME);
		
		if(type.equals("Presentity")){
			if(presentityManager.hasRegister(sipUserName)){
				//��presentity �e�Xunregister
	            if (expiresHeader!=null && expiresHeader.getExpires()==0 ) { 
	                //String fromURL=getKey(registerRequest,"From");
	                System.out.println("DEBUG, PresenceServer, presentityManager.hasRegister(), unregister:"+
	                		sipUserName+","+fromURL);
	                //�N�̷s��Tnotify���Ҧ��q�\�̫� ������presentity
	                //processNotifyAll(sipUserName,)
	                removePresentity(sipUserName); // �qpresentityManager ��serverSubscribeManager������presentity
	                
	                //removeAllSubscriptions(fromURL);
	            }else{
				System.out.println("DEBUG, PresenceServer, presentityManager.hasRegister() ,�w����presentity: " +sipUserName);
				presentityManager.updateRegisteredPresentity(sipUserName, fromURL, Double.parseDouble(Lat), Double.parseDouble(Lng));
	            }
			}
			else{
        	System.out.println("DEBUG, PresenceServer, presentityManager.addPresentity()");
        	presentityManager.addPresentity(fromURL, sipUserName, Double.parseDouble(Lat), Double.parseDouble(Lng));
        	//�ˬd�O�_��subscriber���b���ݭq�\��presentity, �p�G���N����subscribe
        	Vector subscriberList = presentityManager.getSubscriberList(sipUserName);
        	if(subscriberList!=null){
        		for(int i =0;i<subscriberList.size();i++){
        			Subscriber subscriber = (Subscriber)subscriberList.get(i);
        			if(subscriber.isSubscribeDelayed()){
        				try{
        				sendSubscribe(fromURL,"subscribe", false);
        				}catch (Exception ex) {
        					System.out.println("DEBUG, PresenceSever, processRegisterRequest(), sendSubscribe()"  );
        					ex.printStackTrace();
        					
        				}
        				subscriber.setSubscribeDelayed(false);
        			}
        		}
        	}
        	
			}
		}
		else if (type.equals("Watcher")){ //���g�n�� �^�Ǫ���PA username
			System.out.println("DEBUG, PresenceServer, got a watcher: "+sipUserName+","+ fromURL);
			String totalPresentityUserName = presentityManager.searchPresentity(Double.parseDouble(Lat), Double.parseDouble(Lng));
			System.out.println("DEBUG, PresenceServer, presentityManager.searchPresentity(), find: "+ totalPresentityUserName);
			try{
			sendMessage(fromURL,"Result:"+totalPresentityUserName);
			}catch (Exception ex) {
				ex.printStackTrace();
			}
			StringTokenizer st = new StringTokenizer(totalPresentityUserName,";");
			int count = st.countTokens();
			while(st.hasMoreTokens()){
				System.out.println(st.nextToken());
			}  
			//�P�_Wacther�O�_�nunregister
			 if (expiresHeader!=null && expiresHeader.getExpires()==0 ){
		               System.out.println("DEBUG, PresenceServer, processRegisterRequest(), removeAllSubscriptions():"+
		               		sipUserName+","+fromURL);
				 removeAllSubscriptions(fromURL);
			 }

			
				

				
		}
		/*
    	        try {
    	        	System.out.println();
    	            
    	            // WE have to check if the REGISTER is not an unregister!!!
    	            // If yes, we have to remove all the subscriptions of this guy!!!
    	        	//��Y��subscriber(fromURL) �o�XUnregister (expiresHeader = 0), �h�N��subscriber�Ҧ��q�\��T����
    	            ExpiresHeader expiresHeader=(ExpiresHeader)registerRequest.getHeader(ExpiresHeader.NAME);
    	            if (expiresHeader!=null && expiresHeader.getExpires()==0 ) { 
    	                //String fromURL=getKey(registerRequest,"From");
    	                System.out.println("DEBUG, PresenceServer, sendSubscribe(), security:"+
    	                " we try to remove all the subscriptions of "+fromURL);
    	                removeAllSubscriptions(fromURL);
    	            }
    	            else{
    	            	System.out.println("DEBUG, PresenceServer, presentityManager.addPresentity()");
    	            	presentityManager.addPresentity(fromURL, getUsername(fromURL));
    	            }
    	            
    	            /*
    	            if (hasSubscriberWaiting(registerRequest) ) {
    	            	System.out.println("DEBUG, PresenceServer, sendSubscribe(), "+
    	                "  We got some subscribers in waiting... Let's send their SUBSCRIBE...");
    	            		//�^�Ǹ�presentity �q�\����subscriber list
    	                    Vector subscriberList=getSubscriberList(registerRequest);
    	                    //�̧��ˬd��presentity���q�\�̬O�_�q�\����, �p�G�� �h�H�eSubscribe��presentity
    	                    for (int i=0;i<subscriberList.size();i++) {
    	                        Subscriber subscriber=(Subscriber) subscriberList.elementAt(i);
    	                        if (subscriber.isSubscribeDelayed() ) {
    	                            subscriber.setSubscribeDelayed(false);
    	                            
    	                            Request subscribeRequest=subscriber.getSubscribeRequest();
    	                            //HeaderFactory headerFactory=headerFactory.getHeaderFactory();
    	                            // WE have to add a new Header: "Event"
    	                            Header header=headerFactory.createHeader("Event","presence");
    	                            subscribeRequest.setHeader(header);
    	                            
    	                            //sendSubscribeRequest(sipProvider, 
    					//subscribeRequest,subscriber.serverTransaction);
    	                        }
    	                    }
    	            }
    	            
    	        }
    	        catch (Exception ex) {
    	            ex.printStackTrace();
    	        }
    	        */
    	    }
    
    public void addSubscriber(Request subscribeRequest,ServerTransaction serverTransaction,
    	    boolean delayed) {
    	        try{
    	            String presentity=getKey(subscribeRequest,"To");  //��X�n�Q�q�\��presentity URL
    	            
    	            String fromURL=getKey(subscribeRequest,"From"); //��X �q�\�̪�URL

    	            //�P�_ �n�q�\��presentity �O�_�w�g�Q�ӭq�\�̨��o�q�\
    	            if ( presentityManager.hasSubscriber(presentity,fromURL) ) {
    	                System.out.println("DEBUG, PresenceServer, addSubscriber(), the peer: "+fromURL+" has already"+
    	                " subscribed for the presentity "+presentity+"!! Let's update the subscription");
    	                Subscriber subscriber=new Subscriber(fromURL,subscribeRequest,serverTransaction,delayed); 
    		//�إߤ@��Subscriber, �O������URL(fromURL), Request(subscribeRequest), Transaction(serverTransaction)
    	                presentityManager.addSubscriber(getUsername(presentity),presentity,subscriber);  //�O���q�\�� �Ψ�q�\��presentity
    	            }
    	            else { // �p�G�쥻�S�q�\ �h�N�q�\�̥[�J�q�\�W��
    	                Subscriber subscriber=new Subscriber(fromURL,subscribeRequest,serverTransaction,delayed);
    	                presentityManager.addSubscriber(getUsername(presentity),presentity,subscriber);
    	                
    	                //DebugProxy.println("DEBUG, PresenceServer, addSubscriber(), the subscriber"+
    	                //" "+fromURL+" has been added for the presentity: "+presentity );
    	            }
    	        }
    	        catch(Exception e) {
    	        	System.out.println("ERROR, PresenceServer, addSubscriber(), Exception raised:");
    	            e.printStackTrace();
    	        }
    	    }
    
    


    
    //��esubscriber�ҵo�e��subscriber�T��
    public void sendSubscribeRequest(SipProvider sipProvider,
    Request request,ServerTransaction serverTransaction) {
        
       // ProxyUtilities proxyUtilities=proxy.getProxyUtilities();
      //  MessageFactory messageFactory=proxy.getMessageFactory();
       //Registrar registrar=proxy.getRegistrar();
      //  HeaderFactory headerFactory=proxy.getHeaderFactory();
      //  TransactionsMapping transactionsMapping =
      //  proxy.getTransactionsMapping();
        
        try{
             System.out.println("PresenceServer: request:"+request.toString());
            
             // We have to forward the SUBSCRIBE to the peer that is the target!!!
             // Otherwise, we wait for the guy to register
             // First, we test if we have to deal with an unsubscribe
             ExpiresHeader expiresHeader=(ExpiresHeader)request.getHeader(
             ExpiresHeader.NAME);
             
             if (expiresHeader==null || expiresHeader.getExpires()!=0) {
                 
                 String presentity=getKey(request,"To");
                 String presentityUserName = getUsername(presentity);
                 
                 /*
                 Vector contacts=registrar.getContactHeaders(presentity);
                 if ( registrar.hasRegistration(presentity) && contacts!=null &&
                 !contacts.isEmpty()) {
                    addSubscriber(request,serverTransaction,false);
                    //if (presenceServer.hasSubscribeWaiting(request) ) {
                    System.out.println("PresenceServer, sendSubscribe(), "+
                    " The contact is registered, we forward the SUBSCRIBE");
                     
                    // Let's use RequestForwarding:
                    Vector targetURIList=new Vector();
                    //targetURIList=registrar.getContactsURI(request);
                    //  Forward the request statefully to the target:
                    //RequestForwarding requestForwarding=proxy.getRequestForwarding();
                    //requestForwarding.forwardRequest(targetURIList,sipProvider,
                    //request,serverTransaction,true);
                 }
                 else {
                     // WE have to store the information and wait for a REGISTER
                     System.out.println("PresenceServer, sendSubscribe(), "+
                     "The receiver of this SUBSCRIBE is not registered, or does "+
                     "not have any contacts: we store the SUBSCRIBE");
                     // Let's clone it for security:
                     addSubscriber(request,serverTransaction,true);
                 }*/
             }
             else {  
                 // We have an unsubscribe to process
                 String presentity=getKey(request,"To");
                 //Vector contacts=registrar.getContactHeaders(presentity);
                 
                 // We have to remove the subscription!!!
                 System.out.println("PresenceServer, sendSubscribe(), "+
                 " Removing a subscription in progress...");
                 removeSubscriber(request);
                 
                 // WE have to send him back a NOTIFY with body (closed:offline)
                  
             }
              
         }
         catch (Exception ex) {
             ex.printStackTrace();
         }
              
     }
     
	

    
    /** 
     * This method is called by the SIP stack when there's no answer 
     * to a message. Note that this is treated differently from an error
     * message. 
     */
    public void processTimeout(TimeoutEvent evt) {
	messageProcessor
		.processError("Previous message not sent: " + "timeout");
    }

    /** 
     * This method is called by the SIP stack when there's an asynchronous
     * message transmission error.  
     */
    public void processIOException(IOExceptionEvent evt) {
	messageProcessor.processError("Previous message not sent: I/O"
		+ "I/O Exception");
    }

    /** 
     * This method is called by the SIP stack when a dialog (session) ends. 
     */
    public void processDialogTerminated(DialogTerminatedEvent evt) {
    }

    /** 
     * This method is called by the SIP stack when a transaction ends. 
     */
    public void processTransactionTerminated(TransactionTerminatedEvent evt) {
    }

    public String getHost() {
	int port = sipProvider.getListeningPoint().getPort();
	String host = sipStack.getIPAddress();
	return host;
    }

    public int getPort() {
	int port = sipProvider.getListeningPoint().getPort();
	return port;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String newUsername) {
	username = newUsername;
    }

    public MessageProcessor getMessageProcessor() {
	return messageProcessor;
    }

    public void setMessageProcessor(MessageProcessor newMessageProcessor) {
	messageProcessor = newMessageProcessor;
    }

}
