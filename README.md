# Compile project

From command line use:

- To compile and run tests:
`./gradlew clean build`

- To run the app:
`./gradlew run -Dexec.args=absolte/path/to/config`


Execute Rps.java with
'config\1.conf'


# TASKS

- Set thread for periodical ping of Samplers 
- Test pull send-receive
- Work on error of NseCLient if NSE Server is closed

  - MOCKS are in - 
PushSender.sendMyId()
PullClient.makePullRequest()
 
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
    
#TODO

- check if pull list has our peer but not Put it into the list