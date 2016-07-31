# Compile project

From command line use:
`./gradlew clean build`

# TASKS

- Do the file reader
- 


# MessageParser  - methods - 
 - isRPSQuery               ByteBuf -> Boolean [read RPS_Query]
 - buildRpsResponde         Peer -> ByteBuf    [generate RPS_Peer]
 - buildPeerFromGossipPush  ByteBuf -> Peer    [read GossipPush]
 - buildGossipPush          Peer -> ByteBuf    [generate GossipPush]
 - getGossipNotifyForPush       -> ByteBuf     [generate GossipNotify]
 