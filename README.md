# Compile project

From command line use:
`./gradlew clean build`

# TASKS

- Implement PushSender
- Set thread for periodical ping of Samplers 
- Set the file read (FileParser) in RPS


# MessageParser  - methods - 
 - isRPSQuery               ByteBuf -> Boolean [read RPS_Query]
 - buildRpsResponde         Peer -> ByteBuf    [generate RPS_Peer]
 - buildPeerFromGossipPush  ByteBuf -> Peer    [read GossipPush]
 - buildGossipPush          Peer -> ByteBuf    [generate GossipPush]
 - getGossipNotifyForPush       -> ByteBuf     [generate GossipNotify]
 
 
# Modules 
    - PushSender [Controlled by Brahms]
    - PushReceiver [Queried by brahms]
    
    - PullServer [Autonomous]          
    - PullClient [Brahms]
    
    + Brahms - OK
    - GossipSender [Autonomous]
    - QueryServer [connected to Brahms localView]
    + NseClient - OK
    
    - Rps
    
# Steps in Brahms testing

    
    
    Brahms connect to : NseClient - PushSender - Push Receiver

 