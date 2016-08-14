# Compile project

From command line use:
`./gradlew clean build`

Execute Rps.java with
'config\1.conf'


# TASKS

- Set thread for periodical ping of Samplers 
- Test pull send-receive
- Work on error of NseCLient if NSE Server is closed
 
# Modules 
    - PushSender [Controlled by Brahms]
    + PushReceiver [Queried by brahms]
    
    - PullServer [Autonomous]          
    - PullClient [Brahms]
    
    + Brahms - OK
    + GossipSender [Autonomous]

    + QueryServer [connected to Brahms localView]
    
    + NseClient - OK
    
    - Rps
    
#GOSSIP

    - GossipSend
    - GossipNotification to receive 