package io.nuls.network.constant;

public interface NetworkConstant {

    short NETWORK_MODULE_ID = 4;


    /**
     * -----------[netty configs ]------------
     */
    int READ_IDEL_TIME_OUT = 30;
    int WRITE_IDEL_TIME_OUT = 30;
    int ALL_IDEL_TIME_OUT = 30;
    int MAX_FRAME_LENGTH = 10 * 1024 * 1024;
    int CONNETCI_TIME_OUT = 5000;
    int SAME_IP_MAX_COUNT = 10;
    int CONEECT_FAIL_MAX_COUNT = 6;

    /**
     * --------[network configs] -------
     */
    String NETWORK_SECTION = "network";
    String NETWORK_SERVER_PORT = "network.server.port";
    String NETWORK_MAGIC = "network.magic";
    String NETWORK_NODE_MAX_IN = "network.max.in";
    String NETWORK_NODE_MAX_OUT = "network.max.out";
    String NETWORK_SEED_IP = "network.seed.ip";
    String NETWORK_NODE_IN_GROUP = "inGroup";
    String NETWORK_NODE_OUT_GROUP = "outGroup";


    int HANDSHAKE_SEVER_TYPE = 2;
    int HANDSHAKE_CLIENT_TYPE = 1;

    //network message type
    short NETWORK_GET_VERSION = 01;
    short NETWORK_VERSION = 02;
    short NETWORK_GET_NODE = 03;
    short NETWORK_NODE = 04;
    short NETWORK_GET_NODEIP = 05;
    short NETWORK_NODEIP = 06;
    short NETWORK_HANDSHAKE = 07;

}
