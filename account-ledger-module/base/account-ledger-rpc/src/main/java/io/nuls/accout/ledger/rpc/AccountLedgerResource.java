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

/**
 * @author: Facjas
 * @date: 2018/5/8
 */
package io.nuls.accout.ledger.rpc;

import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.accout.ledger.rpc.dto.TransactionInfoDto;
import io.nuls.accout.ledger.rpc.dto.UtxoDto;
import io.nuls.accout.ledger.rpc.form.TransferFeeForm;
import io.nuls.accout.ledger.rpc.form.TransferForm;
import io.nuls.accout.ledger.rpc.util.UtxoDtoComparator;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.ledger.service.LedgerService;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/14.
 */

@Path("/accountledger")
@Api(value = "/accountledger", description = "accountledger")
@Component
public class AccountLedgerResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private LedgerService ledgerService;

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户地址查询账户余额", notes = "result.data: balanceJson 返回对应的余额信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Balance.class)
    })
    public RpcClientResult getBalance(@ApiParam(name = "address", value = "账户地址", required = true)
                                      @PathParam("address") String address) {
        byte[] addressBytes = null;
        try {
            addressBytes = Base58.decode(address);
        } catch (Exception e) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (addressBytes.length != AddressTool.HASH_LENGTH) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Result result = null;
        try {
            result = accountLedgerService.getBalance(addressBytes);
        } catch (NulsException e) {
            e.printStackTrace();
            return Result.getFailed(AccountLedgerErrorCode.UNKNOW_ERROR).toRpcClientResult();
        }

        if (result == null) {
            return Result.getFailed(AccountLedgerErrorCode.UNKNOW_ERROR).toRpcClientResult();
        }
        return result.toRpcClientResult();
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "转账", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transfer(@ApiParam(name = "form", value = "转账", required = true) TransferForm form) {
        if (form == null) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!Address.validAddress(form.getToAddress())) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (form.getAmount() <= 0) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!validTxRemark(form.getRemark())) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Na value = Na.valueOf(form.getAmount());
        return accountLedgerService.transfer(AddressTool.getAddress(form.getAddress()),
                AddressTool.getAddress(form.getToAddress()),
                value, form.getPassword(), form.getRemark()).toRpcClientResult();
    }


    @POST
    @Path("/transfer/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "转账手续费", notes = "result.data: resultJson 返回转账结果")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success")
    })
    public RpcClientResult transferFee(@BeanParam() TransferFeeForm form) {
        if (form == null) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!Address.validAddress(form.getToAddress())) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (form.getAmount() <= 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (!validTxRemark(form.getRemark())) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Na value = Na.valueOf(form.getAmount());
        return accountLedgerService.transferFee(AddressTool.getAddress(form.getAddress()),
                AddressTool.getAddress(form.getToAddress()), value, form.getRemark()).toRpcClientResult();
    }


    @GET
    @Path("/tx/list/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "账户地址查询交易列表", notes = "result.data: balanceJson 返回账户相关的交易列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getTxInfoList(@ApiParam(name = "address", value = "账户地址", required = true)
                                         @PathParam("address") String address,
                                         @ApiParam(name = "type", value = "类型")
                                         @QueryParam("type") Integer type,
                                         @ApiParam(name = "pageNumber", value = "页码")
                                         @QueryParam("pageNumber") Integer pageNumber,
                                         @ApiParam(name = "pageSize", value = "每页条数")
                                         @QueryParam("pageSize") Integer pageSize) {
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (type == null || type <= 0) {
            type = -1;
        }

        byte[] addressBytes = null;
        Result dtoResult = Result.getSuccess();

        try {
            addressBytes = Base58.decode(address);
        } catch (Exception e) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        Result<List<TransactionInfo>> rawResult = accountLedgerService.getTxInfoList(addressBytes);
        if (rawResult.isFailed()) {
            dtoResult.setSuccess(false);
            dtoResult.setErrorCode(rawResult.getErrorCode());
            return dtoResult.toRpcClientResult();
        }

        List<TransactionInfo> result = new ArrayList<TransactionInfo>();
        if (type == -1) {
            result = rawResult.getData();
        } else {
            for (TransactionInfo txInfo : rawResult.getData()) {
                if (txInfo.getTxType() == type) {
                    result.add(txInfo);
                }
            }
        }

        Page<TransactionInfoDto> page = new Page<>(pageNumber, pageSize, result.size());
        int start = pageNumber * pageSize - pageSize;
        if (start >= page.getTotal()) {
            dtoResult.setData(page);
            return dtoResult.toRpcClientResult();
        }

        int end = start + pageSize;
        if (end > page.getTotal()) {
            end = (int) page.getTotal();
        }

        List<TransactionInfoDto> infoDtoList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            TransactionInfo info = result.get(i);
            Transaction tx = ledgerService.getTx(info.getTxHash());
            if (tx == null) {
                tx = accountLedgerService.getUnconfirmedTransaction(info.getTxHash()).getData();
            }
            if (tx == null) {
                continue;
            }
            info.setInfo(tx.getInfo(addressBytes));
            infoDtoList.add(new TransactionInfoDto(info));

        }
        page.setList(infoDtoList);

        dtoResult.setSuccess(true);
        dtoResult.setData(page);
        return dtoResult.toRpcClientResult();
    }

    @GET
    @Path("/utxo/lock/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询用户冻结列表", notes = "result.data: balanceJson 返回账户相关的冻结UTXO列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getLockUtxo(@ApiParam(name = "address", value = "地址")
                                       @PathParam("address") String address,
                                       @ApiParam(name = "pageNumber", value = "页码")
                                       @QueryParam("pageNumber") Integer pageNumber,
                                       @ApiParam(name = "pageSize", value = "每页条数")
                                       @QueryParam("pageSize") Integer pageSize) {
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        byte[] addressBytes = null;
        Result dtoResult = new Result<>();

        try {
            addressBytes = Base58.decode(address);
        } catch (Exception e) {
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        //获取所有锁定的utxo
        Result<List<Coin>> result = accountLedgerService.getLockedUtxo(addressBytes);
        if (result.isFailed()) {
            dtoResult.setSuccess(false);
            dtoResult.setErrorCode(result.getErrorCode());
            return dtoResult.toRpcClientResult();
        }

        List<Coin> coinList = result.getData();
        Page<UtxoDto> page = new Page<>(pageNumber, pageSize, result.getData().size());
        int start = pageNumber * pageSize - pageSize;
        if (start >= coinList.size()) {
            dtoResult.setData(page);
            return dtoResult.toRpcClientResult();
        }

        List<UtxoDto> utxoDtoList = new ArrayList<>();
        byte[] txHash = new byte[NulsDigestData.HASH_LENGTH];
        for (Coin coin : coinList) {
            //找到每一条uxto，对应的交易类型与时间
            System.arraycopy(coin.getOwner(), 0, txHash, 0, NulsDigestData.HASH_LENGTH);
            Transaction tx = ledgerService.getTx(txHash);
            if (tx == null) {
                NulsDigestData hash = new NulsDigestData();
                try {
                    hash.parse(txHash);
                    tx = accountLedgerService.getUnconfirmedTransaction(hash).getData();
                } catch (NulsException e) {
                    Log.error(e);
                    return Result.getFailed(KernelErrorCode.DATA_PARSE_ERROR).toRpcClientResult();
                }
            }
            //考虑到数据回滚，会出现找不到的情况
            if (tx == null) {
                continue;
            }
            utxoDtoList.add(new UtxoDto(coin, tx));
        }

        //重新赋值page对象
        page = new Page<>(pageNumber, pageSize, utxoDtoList.size());
        if (start >= page.getTotal()) {
            dtoResult.setData(page);
            return dtoResult.toRpcClientResult();
        }

        Collections.sort(utxoDtoList, UtxoDtoComparator.getInstance());
        int end = start + pageSize;
        if (end > utxoDtoList.size()) {
            end = utxoDtoList.size();
        }

        page.setList(utxoDtoList.subList(start, end));
        dtoResult.setSuccess(true);
        dtoResult.setData(page);
        return dtoResult.toRpcClientResult();
    }

    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(NulsConfig.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
