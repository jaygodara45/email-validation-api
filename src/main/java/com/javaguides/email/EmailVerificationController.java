package com.javaguides.email;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.naming.NamingEnumeration;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import java.util.Hashtable;
import java.util.regex.Pattern;

@RestController
public class EmailVerificationController {

    @GetMapping("/verifyEmailDomain")
    public String verifyEmailDomain(@RequestParam String email) {
        if (isValidEmailAddress(email)) {
            String domain = getDomainFromEmail(email);
            if (domain != null && !domain.isEmpty()) {
                if (isDomainFormatValid(domain) && isDomainMXValid(domain)!="false" && !"0 .".equals(isDomainMXValid(domain))) {
                    return "Valid email domain: " + domain + " "+isDomainMXValid(domain);
                } else if (isDomainMXValid(domain)=="false") {
                    return "Invalid email domain MX invalid: " + domain + " "+ isDomainMXValid(domain);
                } else {
                    return "Invalid email domain regex invalid: " + domain+ " "+isDomainMXValid(domain);
                }
            } else {
                return "Invalid email domain extraction.";
            }
        } else {
            return "Invalid email address.";
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
            if (attr != null && attr.size() > 0){
                StringBuilder result = new StringBuilder();
                NamingEnumeration<?> enumeration = attr.getAll();
                while (enumeration.hasMore()) {
                    String mx = (String) enumeration.next();
                    result.append(mx);
                }
                return result.toString();
            }
            else{
                return "false";
            }
        } catch (NamingException e) {
            return "false";
        }
    }
}
