package com.wz.crawler;

import jodd.mail.Email;
import jodd.mail.MailServer;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

/**
 * Created by zn on 2018/8/4.
 */
public class EmailUtils {
    public static void sendEmail(@NonNull String toEmail, @NonNull String subject, @NonNull String content){
        if(StringUtils.isBlank(toEmail) || StringUtils.isBlank(subject) || StringUtils.isBlank(content)) {
            return ;
        }
        Email email = Email.create()
                .from("zhangning_holley@126.com")
                .to(toEmail)//wanghua96_1@aliyun.com   ebay@imnavy.com
                .subject(subject)
                .textMessage(content);

        SmtpServer smtpServer = MailServer.create()
                .host("smtp.126.com")
                .ssl(true)
                .port(465)
                .auth("zhangning_holley@126.com","520liuqiumei")
                .buildSmtpMailServer();
        SendMailSession session = smtpServer.createSession();
        session.open();
        session.sendMail(email);

        session.close();

    }

    public static void main(String ... args) {
        //sendEmail("");
    }
}
