package info.onixcoin.desktop;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import static info.onixcoin.desktop.utils.GuiUtils.checkGuiThread;
import java.io.ByteArrayInputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javax.annotation.Nullable;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

public class WalletMpkController {

    private static final Logger log = LoggerFactory.getLogger(WalletMpkController.class);
    public Main.OverlayUI overlayUI;
    private KeyParameter aesKey;
    public Text labelXpub;
    public ImageView qrXpub;
    private String mpk;
    @FXML protected Label copyWidget;
    /**
     * Initializes the controller class.
     */
    // Note: NOT called by FXMLLoader!
    public void initialize(@Nullable KeyParameter aesKey) {
        DeterministicSeed seed = Main.bitcoin.wallet().getKeyChainSeed();
        if (aesKey == null) {
            if (seed.isEncrypted()) {
                log.info("Wallet is encrypted, requesting password first.");
                // Delay execution of this until after we've finished initialising this screen.
                Platform.runLater(() -> askForPasswordAndRetry());
                return;
            }
        } else {
            this.aesKey = aesKey;

        }

        DeterministicKey extendedKey = Main.bitcoin.wallet().getWatchingKey();
        this.mpk = extendedKey.serializePubB58(Main.params);
        //System.out.println("mpk " + mpk);
        // Now we can display the wallet seed as appropriate.
        labelXpub.setText(mpk);

        final byte[] imageBytes = QRCode
                .from(mpk)
                .withSize(260, 260)
                .to(ImageType.PNG)
                .stream()
                .toByteArray();
        Image qrImage = new Image(new ByteArrayInputStream(imageBytes));
        qrXpub.setImage(qrImage);
        
        AwesomeDude.setIcon(copyWidget, AwesomeIcon.COPY);
        Tooltip.install(copyWidget, new Tooltip(Main.resourceBundle.getString("walletspk.tooltip.copyxpub")));
    }

    private void askForPasswordAndRetry() {
        Main.OverlayUI<WalletPasswordController> pwd = Main.instance.overlayUI("wallet_password.fxml");
        pwd.controller.aesKeyProperty().addListener((observable, old, cur) -> {
            // We only get here if the user found the right password. If they don't or they cancel, we end up back on
            // the main UI screen.
            checkGuiThread();
            Main.OverlayUI<WalletMpkController> screen = Main.instance.overlayUI("wallet_mpk.fxml");
            screen.controller.initialize(cur);
        });
    }

    public void closeClicked(ActionEvent event) {
        overlayUI.done();
    }
    
    @FXML
    protected void copyWidgetClicked(MouseEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(this.mpk);
        content.putHtml(String.format("<a href='%s'>%s</a>", this.mpk, this.mpk));
        clipboard.setContent(content);
    }

}
