package bf.kvill.associa.shared.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service dédié à l'envoi d'emails.
 * Utilise JavaMailSender fourni automatiquement par Spring Boot
 * (via la dépendance spring-boot-starter-mail).
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // Injection automatique du bean configuré par Spring Boot
    private final JavaMailSender mailSender;

    // Adresse email par défaut de l'expéditeur (ex: noreply@associa.bf)
    @Value("${spring.mail.username:noreply@associa.bf}")
    private String senderEmail;

    /**
     * Envoie l'email de réinitialisation de mot de passe.
     * La méthode est @Async pour ne pas bloquer le thread HTTP principal.
     * C'est crucial car l'envoi d'email via SMTP peut prendre quelques secondes.
     *
     * @param to        L'adresse email du destinataire
     * @param resetLink Le lien de réinitialisation généré par l'AuthService
     */
    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            log.info("Tentative d'envoi d'email de reset à {}", to);

            // Création d'un message MIME (permet d'envoyer du HTML)
            MimeMessage message = mailSender.createMimeMessage();

            // Le MimeMessageHelper facilite la construction du contenu (HTML, encodage,
            // etc.)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe - Associa");

            // Construction du corps du message en HTML
            String htmlContent = buildResetPasswordHtmlContent(resetLink);
            helper.setText(htmlContent, true); // true = active le format HTML

            // L'envoi SMTP réel
            mailSender.send(message);

            log.info("Email de reset de mot de passe envoyé avec succès à {}", to);
        } catch (MessagingException e) {
            // On catch l'erreur pour que l'action principale (ex: AuthController)
            // ne soit pas bloquée en cas de serveur SMTP injoignable.
            log.error("Erreur lors de l'envoi de l'email de reset à {}: {}", to, e.getMessage());
        }
    }

    /**
     * Construit le template HTML de l'email.
     * Pour un vrai projet, on utiliserait plutôt Thymeleaf
     * (spring-boot-starter-thymeleaf)
     * pour charger des templates HTML depuis src/main/resources/templates.
     */
    private String buildResetPasswordHtmlContent(String resetLink) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "  <h2 style=\"color: #2b6cb0;\">Demande de réinitialisation de mot de passe</h2>\n" +
                "  <p>Bonjour,</p>\n" +
                "  <p>Nous avons reçu une demande de réinitialisation de votre mot de passe pour votre compte <strong>Associa</strong>.</p>\n"
                +
                "  <p>Si vous êtes à l'origine de cette demande, vous pouvez définir un nouveau mot de passe en cliquant sur le bouton ci-dessous :</p>\n"
                +
                "  <div style=\"text-align: center; margin: 30px 0;\">\n" +
                "    <a href=\"" + resetLink + "\" \n" +
                "       style=\"background-color: #2b6cb0; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;\">\n"
                +
                "      Réinitialiser le mot de passe\n" +
                "    </a>\n" +
                "  </div>\n" +
                "  <p style=\"font-size: 14px; color: #718096;\">\n" +
                "    Ce lien est valide pendant 1 heure. Si vous n'avez pas demandé à réinitialiser votre mot de passe, \n"
                +
                "    vous pouvez ignorer cet email en toute sécurité.\n" +
                "  </p>\n" +
                "  <hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 30px 0;\" />\n" +
                "  <p style=\"font-size: 12px; color: #a0aec0; text-align: center;\">\n" +
                "    Ceci est un email automatique, merci de ne pas y répondre.\n" +
                "  </p>\n" +
                "</div>";
    }
}
