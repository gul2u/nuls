package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseNulsEvent<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable{
    private NulsEventHeader header;
    private NulsDigestData hash;
    private NulsSignData sign;
    private T eventBody;

    public BaseNulsEvent(short moduleId, short eventType, byte[] extend) {
        this.header = new NulsEventHeader(moduleId, eventType, extend);
    }

    public BaseNulsEvent(short moduleId, short eventType) {
        this.header = new NulsEventHeader(moduleId, eventType, null);
    }

    @Override
    public final int size() {
        if (eventBody != null) {
            return header.size() + eventBody.size();
        } else {
            return header.size();
        }
    }

    @Override
    public final void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        this.header.serializeToStream(stream);
        if (eventBody != null) {
            this.eventBody.serializeToStream(stream);
        }
    }

    @Override
    public final void parse(NulsByteBuffer byteBuffer) {
        this.header = new NulsEventHeader();
        this.header.parse(byteBuffer);
        this.eventBody = parseEventBody(byteBuffer);
    }

    protected abstract T parseEventBody(NulsByteBuffer byteBuffer);

    public T getEventBody() {
        return eventBody;
    }

    public void setEventBody(T eventBody) {
        this.eventBody = eventBody;
    }

    public NulsEventHeader getHeader() {
        return header;
    }

    public void setHeader(NulsEventHeader header) {
        this.header = header;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }
}
