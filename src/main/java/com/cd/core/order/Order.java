package com.cd.core.order;

import com.cd.core.data.MessageData;
import com.cd.core.pojo.Command;

public interface Order {
    MessageData execute(String handlerId, Command command, String[] params);
}
