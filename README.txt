Instruções de uso:

1 - Inicie o rmiregistry: start rmiregistry
2 - Inicie o servidor: java Peer.Peer <MDB HOST> <MDB PORT> <MDR HOST> <MDR PORT> <MC HOST> <MC PORT> <VERSÃO> <PEER ID> <REMOTE OBJ NAME>
3 - Inicie o client: java Peer.TestApp <REMOTE OBJ NAME> <OPERAÇÃO> <OPERANDOS>, onde <OPERAÇÃO> pode ser BACKUP, RESTORE, DELETE, ...