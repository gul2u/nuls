package io.nuls.kernel.processor;

import io.nuls.kernel.model.Result;

/**
 * 命令行处理接口，其他模块的RPC实现须实现该接口
 */
public interface CommandProcessor {

    String getCommand();

    String getHelp();

    String getCommandDescription();

    boolean argsValidate(String[] args);

    Result execute(String[] args);
}
