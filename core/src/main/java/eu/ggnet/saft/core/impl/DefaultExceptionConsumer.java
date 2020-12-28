/*
 * Copyright (C) 2014 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.ggnet.saft.core.impl;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.UiParent;

/**
 *
 * @author oliver.guenther
 */
public class DefaultExceptionConsumer implements BiConsumer<Optional<UiParent>, Throwable> {

    private final static Logger L = LoggerFactory.getLogger(DefaultExceptionConsumer.class);

    @Override
    public void accept(Optional<UiParent> optParent, Throwable throwable) {
        Throwable b = Objects.requireNonNull(throwable, "Throwable must not be null");
        if ( b instanceof CancellationException || b.getCause() instanceof CancellationException ) {
            L.debug("FinalExceptionConsumer catches CancellationException, which is ignored by default");
            return;
        }
        L.error("Systemfehler: {}", b.getClass().getSimpleName(), b);
        String deepestMessage = extractDeepestMessage(b);
        Objects.requireNonNull(optParent, "optParent must not be null").map(p -> Ui.build().parent(p)).orElse(Ui.build()).title("Systemfehler").swing()
                .show(() -> new DetailView(deepestMessage, getUserInfo() + '\n' + toMultilineStacktraceMessages(b), getUserInfo() + '\n' + toStackStrace(b)));
    }

    private String getUserInfo() {
        String windowsUser = System.getProperty("user.name");
        String host = "Konnte Hostname nicht auslesen";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            host = localHost.getCanonicalHostName();
            host += "/" + localHost.getHostName();
        } catch (UnknownHostException ex) {
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Windows Daten: User=").append(windowsUser).append(" Hostname=").append(host);
        return sb.toString();
    }

    /**
     * Extract the deepest Throwable and return its message.
     *
     * @param ex the exception to parse the stack trace.
     * @return the simple class name and the message of the deepest throwable.
     */
    private String extractDeepestMessage(Throwable ex) {
        if ( ex == null ) return "";
        if ( ex.getCause() == null ) return ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage();
        return extractDeepestMessage(ex.getCause());
    }

    /**
     * Returns all stack trace class simple names and messages as a multiline string.
     *
     * @param ex the exception to start with.
     * @return all messages and class names.
     */
    private String toMultilineStacktraceMessages(Throwable ex) {
        if ( ex == null ) return "";
        if ( ex.getCause() == null ) return ex.getClass().getSimpleName() + ":" + ex.getLocalizedMessage();
        return ex.getClass().getSimpleName() + ":" + ex.getLocalizedMessage() + "\n" + toMultilineStacktraceMessages(ex.getCause());
    }

    public static String toStackStrace(Throwable ex) {
        try (StringWriter sw = new StringWriter()) {
            ex.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

}
