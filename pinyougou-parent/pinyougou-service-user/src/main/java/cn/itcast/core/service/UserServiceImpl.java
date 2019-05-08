package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private UserDao userDao;

    @Override
    public void sendCode(String phone) {

        //获取验证码
        String randomNumeric = RandomStringUtils.randomNumeric(6);

        //把验证码保存到缓存中
        redisTemplate.boundValueOps(phone).set(randomNumeric,8, TimeUnit.HOURS);

        //发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("PhoneNumbers",phone);
                mapMessage.setString("SignName","品优购商城");
                mapMessage.setString("TemplateCode","SMS_164278016");
                mapMessage.setString("TemplateParam","{\"number\":\""+randomNumeric+"\"}");
                return mapMessage;
            }
        });

    }

    @Override
    public void add(User user, String smscode) {

        String cod = (String) redisTemplate.boundValueOps(user.getPhone()).get();

        if (cod!=null){
            if (cod.equals(smscode)){

                user.setCreated(new Date());
                user.setUpdated(new Date());
                //user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                userDao.insert(user);
            }else {
                throw  new RuntimeException("验证码错误");
            }
        }else {
            throw  new RuntimeException("验证码已过期");
        }

    }
}
