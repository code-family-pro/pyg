package cn.itcast.core.listener;

import cn.itcast.core.service.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;


public class PageListener implements MessageListener {
    @Autowired
    StaticPageService staticPageService;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm=(ActiveMQTextMessage)message;

        try {
            String id = atm.getText();
                staticPageService.index(Long.parseLong(id));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
