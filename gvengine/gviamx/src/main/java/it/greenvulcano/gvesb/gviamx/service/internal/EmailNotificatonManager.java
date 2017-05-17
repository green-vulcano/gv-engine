package it.greenvulcano.gvesb.gviamx.service.internal;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import it.greenvulcano.gvesb.gviamx.domain.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.service.NotificationManager;

public class EmailNotificatonManager implements NotificationManager {

	private final static Logger LOG = LoggerFactory.getLogger(EmailNotificatonManager.class);
	
	private JavaMailSender mailSender;	
	private ConfigurationAdmin configAdmin;
	
	private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
		
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}

	@Override
	public void sendNotification(UserActionRequest userActionRequest, String event) {		
		LOG.debug("Sending email for " + event + " request with id "+userActionRequest.getId());
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper messageHelper = new MimeMessageHelper(message);
	
		try {
			
			Optional<Configuration> configuration = Optional.ofNullable(configAdmin.getConfiguration("it.greenvulcano.gvesb.gviamx")).filter(Objects::nonNull);
			
			Dictionary<String, Object> config = configuration.isPresent() ? Optional.ofNullable(configuration.get().getProperties()).orElse(new Hashtable<>()) : new Hashtable<>();
		
			String configPrefix = "gviamx.mail."+event+".";
			
			messageHelper.setFrom(Optional.ofNullable(config.get(configPrefix+"from")).orElseThrow(()-> new MessagingException("Missing 'from' in configuration")).toString());		
			messageHelper.setSubject(Optional.ofNullable(config.get(configPrefix+"subject")).orElseThrow(()-> new MessagingException("Missing 'subject' in configuration")).toString());
		
			String contentPath = Optional.ofNullable(config.get(configPrefix+"content")).orElseThrow(()-> new MessagingException("Missing 'subject' in configuration")).toString();
						
			Mustache content = mustacheFactory.compile(new FileReader(contentPath), configPrefix+"template");
			
			StringWriter contentWriter = new StringWriter();
			content.execute(contentWriter, userActionRequest);
			messageHelper.setText(contentWriter.toString(), true);
			
			messageHelper.setTo(userActionRequest.getEmail());
						
			mailSender.send(message);
			LOG.debug("Email sent successfully for password reset request with id "+userActionRequest.getId());
		} catch (MessagingException|IOException e) {
			LOG.error("Fail to send email for password reset with id "+userActionRequest.getId(),e);
		} 		
		
	}

}
