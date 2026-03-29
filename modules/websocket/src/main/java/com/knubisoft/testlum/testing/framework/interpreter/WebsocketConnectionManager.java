package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;

import java.io.IOException;
import java.util.LinkedList;

public interface WebsocketConnectionManager {

    void sendMessage(WebsocketSend wsSend, String payload) throws IOException;

    LinkedList<String> receiveMessages(WebsocketReceive wsReceive);

    void subscribeTo(String topic);

    void openConnection() throws Exception;

    void closeConnection() throws Exception;

    boolean isConnected();
}
