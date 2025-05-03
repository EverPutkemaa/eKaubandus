package eKaubandus.eKauplus.api.service;



public interface EmailService {

    void sendSimpleMessage(String to, String subject, String text);
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);


}
