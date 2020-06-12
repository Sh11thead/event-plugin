package org.tron.eventplugin;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.logsfilter.IPluginEventListener;
import java.util.Objects;

@Extension
public class MongodbEventListener implements IPluginEventListener {

    private static final Logger log = LoggerFactory.getLogger(MongodbEventListener.class);

    @Override
    public void setServerAddress(String address) {

        if (Objects.isNull(address) || address.length() == 0){
            return;
        }

        MongodbSenderImpl.getInstance().setServerAddress(address);
    }

    @Override
    public void setTopic(int eventType, String topic) {
        MongodbSenderImpl.getInstance().setTopic(eventType, topic);
    }

    @Override
    public void setDBConfig(String dbConfig) {
        MongodbSenderImpl.getInstance().setDBConfig(dbConfig);
    }

    @Override
    public void start() {
        // MessageSenderImpl should never init until server address is set
        MongodbSenderImpl.getInstance().init();
    }

    @Override
    public void handleBlockEvent(Object data) {

        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
    }

    @Override
    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
    }

    @Override
    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
    }

    @Override
    public void handleContractEventTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
    }

    @Override
    public void handleSolidityTrigger(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().getTriggerQueue().offer(data);
    }

    @Override
    public void handleBlockErasedEvent(Object data) {
        if (Objects.isNull(data)){
            return;
        }

        MongodbSenderImpl.getInstance().handleBlockEraseTrigger(data);

    }

    @Override
    public void handleTRC20Event(Object data) {
        if (Objects.isNull(data)){
            return;
        }
        String triggerData =(String) data;
        if (triggerData.contains(Constant.TRC20TRACKER_TRIGGER_NAME)) {
            MongodbSenderImpl.getInstance().handleTrc20Trigger(data);
        }
        else if (triggerData.contains(Constant.TRC20TRACKER_SOLIDITY_TRIGGER_NAME)) {
            MongodbSenderImpl.getInstance().handleTrc20SolidityTrigger(data);
        }
    }

    @Override
    public void handleShieldedTRC20Event(Object data) {
        if (Objects.isNull(data)){
            return;
        }
        String triggerData =(String) data;
        if (triggerData.contains(Constant.SHIELDED_TRC20TRACKER_TRIGGER_NAME)) {
            MongodbSenderImpl.getInstance().handleShieldedTrc20Trigger(data);
        }
        else if (triggerData.contains(Constant.SHIELDED_TRC20SOLIDITYTRACKER_TRIGGER_NAME)) {
            MongodbSenderImpl.getInstance().handleShieldedTrc20SolidityTrigger(data);
        }
    }
}
