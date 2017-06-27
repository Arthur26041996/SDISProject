User manual:

1 - Start at least 2 peers, one for initialization and one for receiving the files.
  java Peer.Peer <MDB HOST> <MDB PORT> <MDR HOST> <MDR PORT> <MC HOST> <MC PORT> <VERSION> <PEER ID> <REM OBJ NAME>
  
  Where,
  <MDB HOST>, <MDR HOST> and <MC HOST> are the Multicast channels host address;
  <MDB PORT>, <MDR PORT> and <MC PORT> are the Multicast channels port;
  <VERSION> is the protocol version;
  <PEER ID> is the unique identifier of the peer;
  <REM OBJ NAME> is the name of the remote object which will be used by the test application to initialize a peer.
  
 
 2 - Start the test application:
  java Peer.TestApp <REM OBJ NAME> <OPERATION> <OPERANDS>
  
  Where,
  <REM OBJ NAME> is the remot object name specified when starting the peer;
  <OPERATION> is the desired operation: 'BACKUP', 'RESTORE', 'DELETE', 'RECLAIM' or 'STATUS';
  <OPERANDS> is the operands needed for the respective operation.
  
