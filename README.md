# Compile project

From command line use:
`./gradlew clean build`

# TASKS

- Think how to set setSizeEstimation, if the NSEClient update automatically the value
- 


# MessageParser  - methods - 
 - isRPSQuery               ByteBuf -> Boolean [read RPS_Query]
 - buildRpsResponde         Peer -> ByteBuf    [generate RPS_Peer]
 - buildPeerFromGossipPush  ByteBuf -> Peer    [read GossipPush]
 - buildGossipPush          Peer -> ByteBuf    [generate GossipPush]
 - getGossipNotifyForPush       -> ByteBuf     [generate GossipNotify]
 
 
# Modules 
    - PushSender
    - PushReceiver [Queried by brahms]
    
    - PullServer [Autonomous]          
    - PullClient [Brahms]
    
    - Brahms - OK
    - GossipSender [Autonomous]
    - QueryServer [connected to Brahms localView]
    - NseClient - OK
    
    - Rps
    
# Steps in Brahms testing

    
    
    Brahms connect to : NseClient - G

 