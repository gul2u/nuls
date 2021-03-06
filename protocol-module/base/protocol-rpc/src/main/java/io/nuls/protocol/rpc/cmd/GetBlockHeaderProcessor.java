package io.nuls.protocol.rpc.cmd;

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.Map;


/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class GetBlockHeaderProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getblockheader";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<hash> | <height> get block header by hash or block height - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getblockheader <hash> | <height>--get the block header with hash or height";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String hash = null;
        long height = 0;

        if (StringUtils.isBlank(args[1])) {
            return CommandResult.getFailed(KernelErrorCode.PARAMETER_ERROR.getMsg());
        }

        try {
            height = Long.parseLong(args[1]);
        } catch (Exception e) {
            hash = args[1];
        }

        RpcClientResult result = null;
        if (hash != null) {
            result = restFul.get("/block/header/hash/" + hash, null);
        } else {
            result = restFul.get("/block/header/height/" + height, null);
        }
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("reward", CommandHelper.naToNuls(map.get("reward")));
        map.put("fee", CommandHelper.naToNuls(map.get("fee")));
        map.put("time", DateUtil.convertDate(new Date((Long) map.get("time"))));
        map.put("roundStartTime", DateUtil.convertDate(new Date((Long) map.get("roundStartTime"))));
        result.setData(map);
        return CommandResult.getResult(result);
    }
}
