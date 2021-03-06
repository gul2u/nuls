package io.nuls.account.storage.po;

import io.nuls.account.model.Alias;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author: Charlie
 * @date: 2018/5/11
 */
public class AliasPo extends BaseNulsData {

    private byte[] address;

    private String alias;

    public AliasPo() {
    }

    public AliasPo(Alias alias) {
        this.address = alias.getAddress();
        this.alias = alias.getAlias().trim();

    }

    public Alias toAlias() {
        return new Alias(this.address, this.getAlias().trim());
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias == null ? null : alias.trim();
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {

    }

    @Override
    public int size() {
        return 0;
    }
}