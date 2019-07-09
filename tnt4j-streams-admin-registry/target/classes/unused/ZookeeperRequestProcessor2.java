/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.JsonRpc;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.RuntimeInformation;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;
import org.apache.curator.framework.CuratorFramework;

/**
 * The type Zookeeper request processor.
 */
public class ZookeeperRequestProcessor2 {
    private static final EventSink LOGGER_ZOOKEEPER = LoggerUtils.getLoggerSink("zookeeperLog"); // NON-NLS

    private CuratorFramework curator;


    public ZookeeperRequestProcessor(CuratorFramework curatorFramework) {
        LOGGER_ZOOKEEPER.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS
        curator = curatorFramework;
    }

    private boolean putToZookeeper(CuratorFramework curatorFramework, String responseDir, String message) {
        LOGGER_ZOOKEEPER.log(OpLevel.INFO, String.format("Sending response to %s", responseDir));
        return CuratorUtils.setData(responseDir, message, curatorFramework);
    }

    private void logSetDataOperation(String opName, boolean wasSent) {
        LOGGER_ZOOKEEPER.log(OpLevel.INFO, String.format("Request %s was sent: %b ", opName, wasSent));
    }


    public void methodSelector(JsonRpc jsonRpcRequest, String responseDir) {
        switch (jsonRpcRequest.getMethod()) {
            case "getThreadDump": { // NON-NLS
                LOGGER_ZOOKEEPER.log(OpLevel.INFO,
                        String.format("Received getThreadDump request to: %s", jsonRpcRequest.getId()));
                String threadDump = RuntimeInformation.getThreadDump();
                boolean wasDataSet = putToZookeeper(curator, responseDir, threadDump);
                logSetDataOperation("getThreadDump", wasDataSet); // NON-NLS
            }
            break;
            default:
                LOGGER_ZOOKEEPER.log(OpLevel.WARNING, "Received malformed request");
                break;
        }
    }

}
