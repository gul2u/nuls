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

package io.nuls.account.ledger.constant;

import io.nuls.kernel.constant.ErrorCode;

/**
 * @author: Niels Wang
 * @date: 2018/5/5
 */
public interface AccountLedgerErrorCode {

    ErrorCode PASSWORD_IS_WRONG = ErrorCode.init("AL000", "50000");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init("AL001", "50001");
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED = ErrorCode.init("AL002", "50002");
    ErrorCode ACCOUNT_EXIST = ErrorCode.init("AL003", "50003");
    ErrorCode ADDRESS_ERROR = ErrorCode.init("AL004", "50004");
    ErrorCode ALIAS_EXIST = ErrorCode.init("AL005", "50005");
    ErrorCode ALIAS_ERROR = ErrorCode.init("AL006", "50006");
    ErrorCode ACCOUNT_ALREADY_SET_ALIAS = ErrorCode.init("AL007", "50007");
    ErrorCode NULL_PARAMETER = ErrorCode.init("AL008", "50008");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("AL009", "50009");
    ErrorCode SUCCESS = ErrorCode.init("AL010", "50010");
    ErrorCode FAILED = ErrorCode.init("AL011", "50011");
    ErrorCode PARAMETER_ERROR = ErrorCode.init("AL012", "50012");
    ErrorCode IO_ERROR = ErrorCode.init("AL013", "50013");
    ErrorCode SOURCE_TX_NOT_EXSITS = ErrorCode.init("AL014", "50014");
    ErrorCode UNKNOW_ERROR = ErrorCode.init("AL015", "50015");
    ErrorCode UTXO_STATUS_CHANGE = ErrorCode.init("AL016", "50016");
}
