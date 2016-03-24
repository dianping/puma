package com.dianping.puma.portal.web;

import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 覆盖Spring的RedirectView，防止重定向时url带上jsessionid
 * 
 * @author wukezhu
 */
public class RedirectView extends org.springframework.web.servlet.view.RedirectView {

    public RedirectView() {
        super();
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible, boolean exposeModelAttributes) {
        super(url, contextRelative, http10Compatible, exposeModelAttributes);
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible) {
        super(url, contextRelative, http10Compatible);
    }

    public RedirectView(String url, boolean contextRelative) {
        super(url, contextRelative);
    }

    public RedirectView(String url) {
        super(url);
    }

    @Override
    protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String targetUrl, boolean http10Compatible)
            throws IOException {
        if (http10Compatible) {
            // Send status code 302 by default.
            response.sendRedirect(targetUrl);
        } else {
            HttpStatus statusCode = getHttp11StatusCode(request, response, targetUrl);
            response.setStatus(statusCode.value());
            response.setHeader("Location", targetUrl);
        }
    }

}
