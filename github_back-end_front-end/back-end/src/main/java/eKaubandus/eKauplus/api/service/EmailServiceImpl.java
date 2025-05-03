package eKaubandus.eKauplus.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService{

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        String subject = "E-Kauplus - Kinnitage oma e-posti aadress";
        String message = "Tere!\n\n"
                + "Aitäh, et registreerusite meie e-kauplusesse. Palun kinnitage oma e-posti aadress, klõpsates alloleval lingil:\n\n"
                + verificationUrl + "\n\n"
                + "Link aegub 24 tunni pärast.\n\n"
                + "Kui te ei registreerunud meie e-kauplusesse, võite selle e-kirja ignoreerida.\n\n"
                + "Lugupidamisega,\nE-Kauplus meeskond";

        sendSimpleMessage(to, subject, message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String subject = "E-Kauplus - Parooli lähtestamine";
        String message = "Tere!\n\n"
                + "Saite selle e-kirja, kuna taotlesite oma parooli lähtestamist. Parooli lähtestamiseks klõpsake alloleval lingil:\n\n"
                + resetUrl + "\n\n"
                + "Link aegub 24 tunni pärast.\n\n"
                + "Kui te ei taotlenud parooli lähtestamist, võite selle e-kirja ignoreerida.\n\n"
                + "Lugupidamisega,\nE-Kauplus meeskond";

        sendSimpleMessage(to, subject, message);
    }
}
