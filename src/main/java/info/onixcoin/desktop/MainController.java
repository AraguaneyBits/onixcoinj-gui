/*
 * Copyright by the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.onixcoin.desktop;

import javafx.beans.binding.Bindings;
import javafx.util.Callback;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.MonetaryFormat;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.fxmisc.easybind.EasyBind;
import info.onixcoin.desktop.controls.ClickableBitcoinAddress;
import info.onixcoin.desktop.controls.NotificationBarPane;
import info.onixcoin.desktop.utils.BitcoinUIModel;
import info.onixcoin.desktop.utils.easing.EasingMode;
import info.onixcoin.desktop.utils.easing.ElasticInterpolator;

import static info.onixcoin.desktop.Main.bitcoin;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.LoggerFactory;


/**
 * Gets created auto-magically by FXMLLoader via reflection. The widget fields are set to the GUI controls they're named
 * after. This class handles all the updates and event handling for the main UI.
 */
public class MainController {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MainController.class);
    public HBox controlsBox;
    public Label balance;
    public Button sendMoneyOutBtn;
    public ClickableBitcoinAddress addressControl;
    
    private BitcoinUIModel model = new BitcoinUIModel();
    private NotificationBarPane.Item syncItem;
    public TableView<Transaction> transactionsList = new TableView<>();
    public TableColumn<Transaction,String> estatusColumn = new TableColumn<>();
    public TableColumn<Transaction,String> descripcionColumn = new TableColumn<>();
    public TableColumn<Transaction,String> cantidadColumn = new TableColumn<>();
    public TableColumn<Transaction,String> txColumn = new TableColumn<>();
    public TableColumn<Transaction,String> fechaColumn = new TableColumn<>();
    
    // Called by FXMLLoader.
    public void initialize() {
        addressControl.setOpacity(0.0);
    }
    
    
    private HostServices hostServices ;

    public HostServices getHostServices() {
        return hostServices ;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices ;
    }

    public void onBitcoinSetup() {
        model.setWallet(bitcoin.wallet());
        addressControl.addressProperty().bind(model.addressProperty());
        balance.textProperty().bind(EasyBind.map(model.balanceProperty(), coin -> MonetaryFormat.BTC.noCode().format(coin).toString()));
        // Don't let the user click send money when the wallet is empty.
        sendMoneyOutBtn.disableProperty().bind(model.balanceProperty().isEqualTo(Coin.ZERO));

        showBitcoinSyncMessage();
        model.syncProgressProperty().addListener(x -> {
            if (model.syncProgressProperty().get() >= 1.0) {
                readyToGoAnimation();
                if (syncItem != null) {
                    syncItem.cancel();
                    syncItem = null;
                }
            } else if (syncItem == null) {
                showBitcoinSyncMessage();
            }
        });

        Bindings.bindContent(transactionsList.getItems(), model.getTransactions());

        txColumn.prefWidthProperty().bind(transactionsList.widthProperty().divide(2));
        cantidadColumn.prefWidthProperty().bind(transactionsList.widthProperty().divide(5));
        estatusColumn.prefWidthProperty().bind(transactionsList.widthProperty().divide(5));
        fechaColumn.prefWidthProperty().bind(transactionsList.widthProperty().divide(5));
        
        txColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                
                Hyperlink hyperlink = new Hyperlink(p.getValue().getHashAsString()); 
//                hyperlink.setStyle("-fx-text-fill: black");
//                hyperlink.setStyle("-fx-background-color: yellow");
                hyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                         getHostServices().showDocument("https://www.onixcoin.info/tx/"+hyperlink.getText());

                    }
                });
                
                return new ReadOnlyObjectWrapper(hyperlink);
            }
        });
         
        cantidadColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        cantidadColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                Coin value = tx.getValue(Main.bitcoin.wallet());
                BigDecimal satoshis = new BigDecimal(value.value);
                BigDecimal amountBTC = satoshis.divide(new BigDecimal(100000000));
                return new ReadOnlyObjectWrapper(amountBTC.toString());
            }
        });
        
        estatusColumn.setStyle("-fx-alignment: CENTER;");
        estatusColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                Coin value = tx.getValue(Main.bitcoin.wallet());
                
                String estatus = Main.resourceBundle.getString("status.sent");
                if(value.isPositive()) {
                    if(tx.isPending()) {
                        estatus = Main.resourceBundle.getString("status.pending");
                    }
                    else {
                        estatus = Main.resourceBundle.getString("status.received");
                    }                    
                }
                else if (value.isNegative()) {
                   estatus = Main.resourceBundle.getString("status.sent");
                }  
                return new ReadOnlyObjectWrapper(estatus);
            }
        });
        descripcionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                Coin value = tx.getValue(Main.bitcoin.wallet());
                String descripcion = "";
                if(value.isPositive()) {

                }
                else if (value.isNegative()) {
                    Address address = tx.getOutput(0).getAddressFromP2PKHScript(Main.params);
                    descripcion = address!= null ? address.toString() : "";
                }
                
                return new ReadOnlyObjectWrapper(descripcion);
            }
        });
        
        fechaColumn.setStyle("-fx-alignment: CENTER;");
        fechaColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                DateFormat dateFormat = new SimpleDateFormat(Main.resourceBundle.getString("app.date.format"));
                return new ReadOnlyObjectWrapper(dateFormat.format(tx.getUpdateTime()));
            }
        });
        

    }

    private void showBitcoinSyncMessage() {
        syncItem = Main.instance.notificationBar.pushItem(Main.resourceBundle.getString("app.sync"), model.syncProgressProperty());
    }

    public void sendMoneyOut(ActionEvent event) {
        // Hide this UI and show the send money UI. This UI won't be clickable until the user dismisses send_money.
        Main.instance.overlayUI("send_money.fxml");
    }

    public void settingsClicked(ActionEvent event) {
        Main.OverlayUI<WalletSettingsController> screen = Main.instance.overlayUI("wallet_settings.fxml");
        screen.controller.initialize(null);
    }
    
    public void viewMpk(ActionEvent event) {
        Main.OverlayUI<WalletMpkController> screen = Main.instance.overlayUI("wallet_mpk.fxml");
        screen.controller.initialize(null);
    }
 
    
    public void about(ActionEvent event) {
         Main.instance.overlayUI("wallet_about.fxml");
    }
    
    public void report(ActionEvent event) {
        getHostServices().showDocument("https://github.com/jestevez/onixcoinj-gui/issues");
    }
    public void website(ActionEvent event) {
        getHostServices().showDocument("https://www.onixcoin.info");
    }
     public void exit(ActionEvent event) {
        try {
            Main.instance.stop();
        } catch (Exception ex) {
            LOG.error("fail exit system... {}", ex);
        }
    }

    public void restoreFromSeedAnimation() {
        // Buttons slide out ...
        TranslateTransition leave = new TranslateTransition(Duration.millis(1200), controlsBox);
        leave.setByY(80.0);
        leave.play();
    }

    public void readyToGoAnimation() {
        // Buttons slide in and clickable address appears simultaneously.
        TranslateTransition arrive = new TranslateTransition(Duration.millis(1200), controlsBox);
        arrive.setInterpolator(new ElasticInterpolator(EasingMode.EASE_OUT, 1, 2));
        arrive.setToY(0.0);
        FadeTransition reveal = new FadeTransition(Duration.millis(1200), addressControl);
        reveal.setToValue(1.0);
        ParallelTransition group = new ParallelTransition(arrive, reveal);
        group.setDelay(NotificationBarPane.ANIM_OUT_DURATION);
        group.setCycleCount(1);
        group.play();
    }

    public DownloadProgressTracker progressBarUpdater() {
        return model.getDownloadProgressTracker();
    }
}
