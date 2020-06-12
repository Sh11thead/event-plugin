package org.tron.common.logsfilter;

import org.pf4j.ExtensionPoint;

public interface IPluginEventListener extends ExtensionPoint {
    void setServerAddress(String address);

    void setTopic(int eventType, String topic);

    void setDBConfig(String dbConfig);

    // start should be called after setServerAddress, setTopic, setDBConfig
    void start();

    void handleBlockEvent(Object data);

    void handleTransactionTrigger(Object data);

    void handleContractLogTrigger(Object data);

    void handleContractEventTrigger(Object data);

    void handleSolidityTrigger(Object trigger);

    //Added For trc20

    void handleBlockErasedEvent(Object trigger);

    void handleTRC20Event(Object trigger);

    void handleShieldedTRC20Event(Object trigger);


}
