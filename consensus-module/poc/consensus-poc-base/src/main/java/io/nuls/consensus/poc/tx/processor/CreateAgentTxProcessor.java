/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.validate.ValidateResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ln
 * @date 2018/5/10
 */
@Component
public class CreateAgentTxProcessor implements TransactionProcessor<CreateAgentTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Override
    public Result onRollback(CreateAgentTransaction tx, Object secondaryData) {
        Agent agent = tx.getTxData();
        agent.setTxHash(tx.getHash());

        boolean success = agentStorageService.delete(agent.getTxHash());
        return new Result(success, null);
    }

    @Override
    public Result onCommit(CreateAgentTransaction tx, Object secondaryData) {
        Agent agent = tx.getTxData();
        BlockHeader header = (BlockHeader) secondaryData;
        agent.setTxHash(tx.getHash());
        agent.setBlockHeight(header.getHeight());
        agent.setTime(tx.getTime());

        AgentPo agentPo = PoConvertUtil.agentToPo(agent);

        boolean success = agentStorageService.save(agentPo);
        return new Result(success, null);
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        // Conflict detection, detecting whether the client and the packager repeat
        // 冲突检测，检测委托人和打包人是否重复
        if (null == txList || txList.isEmpty()) {
            return ValidateResult.getSuccessResult();
        }

        Set<String> addressHexSet = new HashSet<>();

        for (Transaction transaction : txList) {
            switch (transaction.getType()) {
                case ConsensusConstant.TX_TYPE_REGISTER_AGENT:
                    CreateAgentTransaction createAgentTransaction = (CreateAgentTransaction) transaction;

                    Agent agent = createAgentTransaction.getTxData();

                    String agentAddressHex = Hex.encode(agent.getAgentAddress());
                    String packAddressHex = Hex.encode(agent.getPackingAddress());

                    if (!addressHexSet.add(agentAddressHex) || !addressHexSet.add(packAddressHex)) {
                        return (ValidateResult) ValidateResult.getFailedResult(getClass().getName(), KernelErrorCode.FAILED, "there is a agent has same address!").setData(transaction);
                    }
                    break;
                case ConsensusConstant.TX_TYPE_RED_PUNISH:
//todo                    RedPunishTransaction redPunishTransaction = (RedPunishTransaction) transaction;
//                    RedPunishData redPunishData = redPunishTransaction.getTxData();
//                    String addressHex = Hex.encode(redPunishData.getAddress());
//                    if (!addressHexSet.add(addressHex)) {
//                        return (ValidateResult) ValidateResult.getFailedResult(getClass().getName(), PocConsensusErrorCode.LACK_OF_CREDIT, "there is a new Red Punish Transaction!").setData(transaction);
//                    }
//                    break;
            }
        }

        return ValidateResult.getSuccessResult();
    }
}