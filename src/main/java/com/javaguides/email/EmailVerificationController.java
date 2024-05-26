package com.javaguides.email;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.regex.Pattern;

@RestController
public class EmailVerificationController {

    @GetMapping("/verifyEmailDomain")
    public ResponseEntity<EmailVerificationResponse> verifyEmailDomain(@RequestParam String email) {
        EmailVerificationResponse response = new EmailVerificationResponse();

        if (isValidEmailAddress(email)) {
            String domain = getDomainFromEmail(email);
            if (domain != null && !domain.isEmpty()) {
                if (isDomainFormatValid(domain)) {
                    String mxValidation = isDomainMXValid(domain);
                    if (!"false".equals(mxValidation) && !"0 .".equals(mxValidation)) {
                        response.setStatus("valid");
                        response.setData(isDomainMXValid(domain));
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    } else {
                        response.setStatus("invalid");
                        response.setData("Invalid MX records");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                } else {
                    response.setStatus("invalid");
                    response.setData("Invalid domain format");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            } else {
                response.setStatus("invalid");
                response.setData("Invalid domain extraction");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } else {
            response.setStatus("invalid");
            response.setData("Invalid email address");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    private boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

    private String getDomainFromEmail(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex > -1) {
            return email.substring(atIndex + 1);
        }
        return null;
    }

    private boolean isDomainFormatValid(String domain) {
        String domainRegex = "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.[A-Za-z0-9-]{1,63}(?<!-))*\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(domainRegex);
        return pattern.matcher(domain).matches();
    }

    private String isDomainMXValid(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes("dns:/" + domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");
            if (attr != null && attr.size() > 0) {
                StringBuilder result = new StringBuilder();
                NamingEnumeration<?> enumeration = attr.getAll();
                while (enumeration.hasMore()) {
                    String mx = (String) enumeration.next();
                    result.append(mx).append(" ");
                }
                return result.toString().trim();
            } else {
                return "false";
            }
        } catch (NamingException e) {
            return "false";
        }
    }

    public static class EmailVerificationResponse {
        private String status;
        private String data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
