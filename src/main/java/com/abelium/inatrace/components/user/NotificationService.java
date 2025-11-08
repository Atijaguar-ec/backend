package com.abelium.inatrace.components.user;

import com.abelium.inatrace.components.common.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.util.Locale;

@Lazy
@Service
public class NotificationService extends BaseService {
	
    private static final Locale LOCALE_ES = new Locale("es", "ES");

    @Value("${INATrace.info.mail}")
    private String infoMail;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public String createEmailConfirmationEmail(String name, String surname, String link) {
        Context context = new Context(LOCALE_ES);
        context.setVariable("heading", true);
        context.setVariable("greetingsTitle", "Gracias por registrarte y bienvenido/a a INATrace Ecuador");
        context.setVariable("greeting", "Hola " + name + " " + surname);
        context.setVariable("message", 
        	"<p>Hemos recibido tus datos y nos pondremos en contacto contigo lo antes posible.</p>" + 
	        "<p>En cuanto verifiquemos y activemos tu perfil, podrás iniciar sesión en tu panel de INATrace ec con el correo y la contraseña que usaste para registrarte.</p>");
        context.setVariable("signatureMessage", "");
        context.setVariable("signature", "Saludos cordiales,");
        context.setVariable("INATraceTeam", "Equipo INATrace ec");
        context.setVariable("link", link);
        context.setVariable("linkText", "CONFIRMAR CORREO");
        return templateEngine.process("inline/basic-ar.html", context); 
    }

    public String createConfirmationEmail(String name, String surname) {
        Context context = new Context(LOCALE_ES);
        context.setVariable("heading", true);
        context.setVariable("greetingsTitle", "¡Tu registro está completo!");
        context.setVariable("greeting", "Hola " + name + " " + surname);
        context.setVariable("message", 
        	"<p>Tu cuenta ha sido verificada correctamente. ¡Felicidades por dar el primer paso hacia la transparencia para mejores resultados!</p>" + 
	        "<p>Si necesitas ayuda, contáctanos a través del chat de soporte en tu panel de INATrace ec" +
	        "(botón verde en la esquina inferior derecha) o escríbenos a <a href=\"mailto:" + infoMail + "\">" + infoMail + "</a> y " +
	        "nos pondremos en contacto contigo lo antes posible.</p>");
        context.setVariable("signatureMessage", "¡Esperamos conversar contigo pronto!");
        context.setVariable("signature", "Saludos cordiales,");
        context.setVariable("INATraceTeam", "Equipo INATrace ec");
        return templateEngine.process("inline/basic-ar.html", context); 
    }
    
    public String createPasswordResetEmail(String link) {
        Context context = new Context(LOCALE_ES);
        context.setVariable("heading", true);
        context.setVariable("greetingsTitle", "Restablecer contraseña de tu cuenta INATrace ec");
        context.setVariable("greeting", "");
        context.setVariable("message", 
        	"<p>Haz clic en el botón de abajo para restablecer la contraseña de tu cuenta INATrace ec.</p>" +
		"<p>Si no solicitaste una nueva contraseña, por favor ignora o elimina este correo.</p>" +
	    	"<p>Si el botón no funciona, copia y pega el siguiente enlace en tu navegador: " +
	    	"<a href=\"" + link + "\">" + link + "</a>" +
	    	"</p>");
        context.setVariable("signatureMessage", "");
        context.setVariable("signature", "Saludos cordiales,");
        context.setVariable("INATraceTeam", "Equipo INATrace ec");
        context.setVariable("link", link);
        context.setVariable("linkText", "RESTABLECER CONTRASEÑA");
        return templateEngine.process("inline/basic-ar.html", context); 
    }    
	
}
