/**
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
 */
package io.nuls.contract.service;

import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.entity.form.ContractCall;
import io.nuls.contract.entity.form.ContractCreate;
import io.nuls.contract.entity.form.ContractDelete;
import io.nuls.contract.entity.form.ContractUpdate;
import io.nuls.kernel.model.Result;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/5
 */
public interface ContractService {

    Result<ContractResult> createContract(ContractCreate create);

    Result<ContractResult> callContract(ContractCall call);

    Result<ContractResult> deleteContract(ContractDelete delete);

    Result<ContractResult> updateContract(ContractUpdate update);

    Result<Object> getContractInfo(String address);

    Result<Object> getVmStatus();
}
