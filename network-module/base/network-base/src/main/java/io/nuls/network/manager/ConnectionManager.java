package io.nuls.network.manager;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.connection.netty.NettyServer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    private static ConnectionManager instance = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    private NetworkParam network = NetworkParam.getInstance();

    private NettyServer nettyServer;

    private NodeManager nodeManager;

    private BroadcastHandler broadcastHandler;

    private NetworkMessageHandlerFactory messageHandlerFactory = NetworkMessageHandlerFactory.getInstance();

    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    public void init() {
        nodeManager = NodeManager.getInstance();
        broadcastHandler = BroadcastHandler.getInstance();
        nettyServer = new NettyServer(network.getPort());
        nettyServer.init();
//        eventBusService = NulsContext.getServiceBean(EventBusService.class);
//        messageHandlerFactory = network.getMessageHandlerFactory();
    }

    public void start() {
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node connection", new Runnable() {
            @Override
            public void run() {
                try {
                    nettyServer.start();
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }, false);
    }

    public void connectionNode(Node node) {

        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node connection", new Runnable() {
            @Override
            public void run() {
                node.setStatus(Node.WAIT);
                NettyClient client = new NettyClient(node);
                client.start();
            }
        }, true);
    }

    public void receiveMessage(ByteBuffer buffer, Node node) throws NulsException {
        List<BaseMessage> list;
        try {
            buffer.flip();
            if (!node.isAlive()) {
                buffer.clear();
                return;
            }
            list = new ArrayList<>();
            byte[] bytes = buffer.array();
            int offset = 0;
            while (offset < bytes.length - 1) {
                MessageHeader header = new MessageHeader();
                header.parse(bytes);
                BaseMessage message = getMessageBusService().getMessageInstance(header.getModuleId(), header.getMsgType()).getData();
                message.parse(bytes);

                list.add(message);
                offset = message.serialize().length;
                if (bytes.length > offset) {
                    byte[] subBytes = new byte[bytes.length - offset];
                    System.arraycopy(bytes, offset, subBytes, 0, subBytes.length);
                    bytes = subBytes;
                    offset = 0;
                }
            }
            for (BaseMessage message : list) {
                if (MessageFilterChain.getInstance().doFilter(message)) {
                    MessageHeader header = message.getHeader();

                    if (node.getMagicNumber() == 0L) {
                        node.setMagicNumber(header.getMagicNumber());
                    }

                    processMessage(message, node);
                } else {
                    node.setStatus(Node.BAD);
                    Log.info("-------------------- receive message filter remove node ---------------------------" + node.getId());
                    nodeManager.removeNode(node.getId());
                }
            }
        } catch (Exception e) {
            throw new NulsException(KernelErrorCode.DATA_ERROR);
        } finally {
            buffer.clear();
        }
    }


    private void processMessage(BaseMessage message, Node node) {
        if (message == null) {
            return;
        }

        if (isNetworkMessage(message)) {
            if (node.getStatus() != Node.HANDSHAKE && !isHandShakeMessage(message)) {
                return;
            }
            asynExecute(message, node);
        } else {
            if (!node.isHandShake()) {
                return;
            }
            messageBusService.receiveMessage(message, node);
        }
    }

    private void asynExecute(BaseMessage message, Node node) {
        BaseNetworkMeesageHandler handler = messageHandlerFactory.getHandler(message);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkEventResult messageResult = handler.process(message, node);
                    processMessageResult(messageResult, node);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(e);
                }
            }

            @Override
            public String toString() {
                StringBuilder log = new StringBuilder();
                log.append("event: " + message.toString())
                        .append(", hash: " + message.getHash())
                        .append(", Node: " + node.toString());
                return log.toString();
            }
        });
    }

    public void processMessageResult(NetworkEventResult messageResult, Node node) throws IOException {
        if (node.getStatus() == Node.CLOSE) {
            return;
        }
        if (messageResult == null || !messageResult.isSuccess()) {
            return;
        }
        if (messageResult.getReplyMessage() != null) {
            broadcastHandler.broadcastToNode((BaseMessage) messageResult.getReplyMessage(), node, true);
        }
    }

    private boolean isNetworkMessage(BaseMessage message) {
        return message.getHeader().getModuleId() == NetworkConstant.NETWORK_MODULE_ID;
    }

    private boolean isHandShakeMessage(BaseMessage message) {
        if (message.getHeader().getMsgType() == NetworkConstant.NETWORK_HANDSHAKE) {
            return true;
        }
        return false;
    }

    public MessageBusService getMessageBusService() {
        if (messageBusService == null) {
            messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        }
        return messageBusService;
    }
}
