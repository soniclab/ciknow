package ciknow.mail;

import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Node;
import ciknow.domain.Survey;
import ciknow.util.Beans;
import ciknow.util.Constants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.AbstractResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.WebApps;

/**
 *
 * @author gyao
 */
public class Mailer {

    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private SurveyDao surveyDao;
    private NodeDao nodeDao;
    private char sent = 'i'; // inital status
    private static Log logger = LogFactory.getLog(Mailer.class);

    public static void main(String[] args) {
        Beans.init();
        Mailer mailer = (Mailer) Beans.getBean("mailService");
        Node node = new Node();
        node.setUsername("gyao");
        node.setEmail("yao.gyao@gmail.com");
        node.setFirstName("York");
        node.setLastName("Yao");
        node.setPassword("secret");

        Survey survey = new Survey();
        survey.setName("DML");
        survey.setDescription("Digital Media Library");
        mailer.sendInvitations(node, survey, "test subject", "yao.gyao@gmail.com");

    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public SurveyDao getSurveyDao() {
        return surveyDao;
    }

    public void setSurveyDao(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void sendInvitations(final Node node, final Survey survey, final String subject, final String fromEmail) {

        MimeMessagePreparator preprarator = new MimeMessagePreparator() {

            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                logger.info("preparing mail...");
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                /*
                 * String adminEmail =
                 * survey.getAttribute(Constants.SURVEY_ADMIN_EMAIL); if
                 * (adminEmail == null || adminEmail.trim().length() < 3 ||
                 * adminEmail.indexOf("@") == -1){ adminEmail =
                 * "no-reply@sonic.northwestern.edu"; }
                 */
                message.setFrom(fromEmail);
                message.addTo(node.getEmail());
                message.setSubject(subject);
                Map model = new HashMap();
                model.put("user", node);
                model.put("survey", survey);

                //GenericRO genericRO = (GenericRO) Beans.getBean("genericRO");				
                //model.put("url", genericRO.getBaseURL());
                WebApp app = WebApps.getCurrent();
                model.put("url", app.getAttribute(Constants.APP_BASE_URL));

                String text = VelocityEngineUtils.mergeTemplateIntoString(
                        velocityEngine,
                        "template.vm",
                        model);
                message.setText(text);

                logger.debug(mimeMessage.getFrom()[0]);
                logger.debug(mimeMessage.getAllRecipients()[0]);
                logger.debug(mimeMessage.getSubject());
                //logger.debug("mail text: \n" + text);

            }
        };

        this.mailSender.send(preprarator);
        logger.info("mail sent.");
    }

    public void send(final String fromEmail, final String toEmail, final String subject, final String msg) {

        MimeMessagePreparator preprarator = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setFrom(fromEmail);
                message.addTo(toEmail);
                message.setSubject(subject);
                message.setText(msg);

            }
        };

        this.mailSender.send(preprarator);
        StringBuilder sb = new StringBuilder();
        sb.append("\nFrom: ").append(fromEmail).append("\n");
        sb.append("To: ").append(toEmail).append("\n");
        sb.append("Subject: ").append(subject).append("\n");
        sb.append("Msg: ").append(msg).append("\n\n");
        logger.info(sb.toString());
    }

    public void sendHTML(final String fromEmail, final String reply, final String[] toEmail, final String subject, final String msg, final String fileName, final String htmlStr) {

        try {
            MimeMessage message = getMailSender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setReplyTo(reply);
            try {
                helper.setTo(toEmail);

            } catch (Exception e) {
                sent = 't';// to email string incorrect
            }

            helper.setSubject(subject);
            helper.setText(msg);

            InputStream attach = new ByteArrayInputStream(htmlStr.getBytes("UTF-8"));
            StreamAttachmentDataSource datasource = new StreamAttachmentDataSource(attach, "attachment", "txt/plain");
            helper.addAttachment(fileName, datasource);
            getMailSender().send(message);
            sent = 's';//success
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public char getMailStatus() {
        return sent;
    }

    class StreamAttachmentDataSource extends AbstractResource {

        private ByteArrayOutputStream outputStream;
        private String name;
        private String contentType;

        public StreamAttachmentDataSource(InputStream inputStream, String name,
                String contentType) {
            this.outputStream = new ByteArrayOutputStream();
            this.name = name;
            this.contentType = contentType;

            int read;
            byte[] buffer = new byte[256];
            try {
                while ((read = inputStream.read(buffer)) != -1) {
                    getOutputStream().write(buffer, 0, read);
                }
            } catch (IOException e) {
                logger.error("Cannot create inputstream for mail attachment");

            }
        }

        @Override
        public String getDescription() {
            return "Stream resource used for attachments";
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(this.outputStream.toByteArray());
        }

        public String getContentType() {
            return contentType;
        }

        public String getName() {
            return name;
        }

        public ByteArrayOutputStream getOutputStream() {
            return outputStream;
        }
    }
}
