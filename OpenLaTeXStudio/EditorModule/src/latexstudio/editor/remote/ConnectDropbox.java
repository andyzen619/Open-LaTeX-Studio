/* 
 * Copyright (c) 2015 Sebastian Brudzinski
 * 
 * See the file LICENSE for copying permission.
 */
package latexstudio.editor.remote;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import latexstudio.editor.settings.ApplicationSettings;
import latexstudio.editor.settings.SettingsService;
import latexstudio.editor.util.PropertyService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Remote",
        id = "latexstudio.editor.remote.ConnectDropbox"
)
@ActionRegistration(
        displayName = "#CTL_ConnectDropbox"
)
@ActionReference(path = "Menu/Remote", position = 3333, separatorAfter = 3383)
@Messages("CTL_ConnectDropbox=Connect to Dropbox")
public final class ConnectDropbox implements ActionListener {
    
    private final static String PROPETIES = "dropbox.properties";
    
    private final static String APP_KEY = PropertyService.
            readProperties(PROPETIES).getProperty("dropbox.appKey");
    private final static String APP_SECRET = PropertyService.
            readProperties(PROPETIES).getProperty("dropbox.appSecret");

    @Override
    public void actionPerformed(ActionEvent e) {
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(DbxUtil.getDbxConfig(), appInfo);
        final String authorizeUrl = webAuth.start();

        class OpenUrlAction implements ActionListener {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    open(new URI(authorizeUrl));
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        JButton button = new JButton();
        button.setText("Connect to Dropbox");
        button.setToolTipText(authorizeUrl);
        button.addActionListener(new OpenUrlAction());
        
        String userToken = JOptionPane.showInputDialog(
            null, 
            button,
            "Authentication token required",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (userToken != null && !userToken.isEmpty()) {
            try {
                DbxAuthFinish authFinish = webAuth.finish(userToken);
                userToken = authFinish.accessToken;
                
                ApplicationSettings appSettings = SettingsService.loadApplicationSettings();
                appSettings.setDropboxToken(userToken);
                SettingsService.saveApplicationSettings(appSettings);
            } catch (DbxException ex) {
                JOptionPane.showMessageDialog(null,
                        "Invalid access token! Open LaTeX Studio has NOT been connected with Dropbox.\n Please try again and provide correct access token.",
                        "Invalid token",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static void open(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
}