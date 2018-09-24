package com.ugnich.templemail;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Simple templates for emails https://github.com/ugnich/TempleMail MIT License
 *
 * @version 1.0
 */
public class TempleMail {

    private Session ss;
    private Transport tr;
    private MimeMessage msg;
    private String template;
    private HashMap<String, String> replacevalues;

    public TempleMail() {
        Properties props = new Properties();
        props.put("mail.host", "localhost");
        props.put("mail.transport.protocol", "smtp");
        ss = Session.getInstance(props, null);

        msg = new MimeMessage(ss);
        replacevalues = new HashMap<String, String>();
    }

    public void setFrom(String email, String name) {
        try {
            msg.setFrom(new InternetAddress(email, name));
        } catch (Exception e) {
            System.err.println("TempleMail.setFrom: " + e.toString());
        }
    }

    public void setTo(String email, String name) {
        try {
            InternetAddress to[] = new InternetAddress[1];
            if (name != null && !name.isEmpty()) {
                to[0] = new InternetAddress(email, name);
            } else {
                to[0] = new InternetAddress(email);
            }
            msg.setRecipients(Message.RecipientType.TO, to);
        } catch (Exception e) {
            System.err.println("TempleMail.setTo: " + e.toString());
        }
    }

    public void setReplyTo(String email, String name) {
        try {
            msg.setReplyTo(new InternetAddress[]{new InternetAddress(email, name)});
        } catch (Exception e) {
            System.err.println("TempleMail.setReplyTo: " + e.toString());
        }
    }

    public void setSubject(String subject) {
        try {
            msg.setSubject(subject);
        } catch (Exception e) {
            System.err.println("TempleMail.setSubject: " + e.toString());
        }
        setReplaceValue("*|SUBJECT|*", subject);
    }

    public void setHeader(String name, String value) {
        try {
            msg.setHeader(name, value);
        } catch (Exception e) {
            System.err.println("TempleMail.setHeader: " + e.toString());
        }
    }

    public void setListUnsubscribeURL(String url) {
        setHeader("List-Unsubscribe", "<" + url + ">");
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setBody(String body) {
        replaceOnce("*|BODY|*", body);
    }

    public void enableFeature(String feature) {
        replaceOnce("<!--*" + feature + "*>", "");
        replaceOnce("<!*" + feature + "*-->", "");
    }

    public void replaceOnce(String key, String value) {
        if (template != null) {
            template = template.replace(key, value);
        }
    }

    public void setReplaceValue(String key, String value) {
        replacevalues.put(key, value);
    }

    public boolean compileMessage() {
        String html = template;
        for (Map.Entry<String, String> entry : replacevalues.entrySet()) {
            html = html.replace(entry.getKey(), entry.getValue());
        }

        boolean ret = false;
        try {
            msg.setText(html, "utf-8", "html");
            ret = true;
        } catch (Exception e) {
            System.err.println("TempleMail.compileMessage: " + e.toString());
        }

        return ret;
    }

    public boolean connectSend() {
        boolean ret = false;
        try {
            Transport.send(msg);
            ret = true;
        } catch (Exception e) {
            System.err.println("TempleMail.connectSend: " + e.toString());
        }
        return ret;
    }

    public boolean connect() {
        boolean ret = false;
        try {
            tr = ss.getTransport();
            tr.connect();
            ret = true;
        } catch (Exception e) {
            System.err.println("TempleMail.connect: " + e.toString());
        }
        return ret;
    }

    public boolean isConnected() {
        return tr != null && tr.isConnected();
    }

    public boolean send() {
        boolean ret = false;

        if (!isConnected()) {
            if (!connect()) {
                System.err.println("TempleMail.send: can't reconnect");
                return false;
            }
        }

        try {
            msg.saveChanges();
            tr.sendMessage(msg, msg.getAllRecipients());
            ret = true;
        } catch (Exception e) {
            System.err.println("TempleMail.send: " + e.toString());
        }
        return ret;
    }

    public boolean disconnect() {
        boolean ret = false;
        try {
            tr.close();
            ret = true;
        } catch (Exception e) {
            System.err.println("TempleMail.disconnect: " + e.toString());
        }
        return ret;
    }
}
