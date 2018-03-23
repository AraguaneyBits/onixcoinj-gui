package info.onixcoin.desktop;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletAboutController implements Initializable {
    
    private static final Logger log = LoggerFactory.getLogger(WalletAboutController.class);
    public Main.OverlayUI overlayUI;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    private HostServices hostServices ;

    public HostServices getHostServices() {
        return hostServices ;
    }
     public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices ;
    }

    
    public void closeClicked(ActionEvent event) {
        overlayUI.done();
    }
    
    public void goAraguaneyBits(ActionEvent event) {
         Main.instance.getMainHostServices().showDocument("https://www.araguaneybits.com");
    }
    public void goGithub(ActionEvent event) {
         Main.instance.getMainHostServices().showDocument("https://github.com/jestevez/onixcoinj-gui");
    }
    public void goYeyoPage(ActionEvent event) {
         Main.instance.getMainHostServices().showDocument("https://www.joseluisestevez.com");
    }
    
}
