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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Gets created auto-magically by FXMLLoader via reflection. The widget fields are set to the GUI controls they're named
 * after. This class handles all the updates and event handling for the main UI.
 */
public class MainController {
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

        
        
        txColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                
                Hyperlink hyperlink = new Hyperlink(p.getValue().getHashAsString()); 
                hyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                         getHostServices().showDocument("https://www.onixcoin.info/tx/"+hyperlink.getText());

                    }
                });
                
                return new ReadOnlyObjectWrapper(hyperlink);
            }
        });
         
        
        cantidadColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                Coin value = tx.getValue(Main.bitcoin.wallet());
                return new ReadOnlyObjectWrapper(MonetaryFormat.BTC.format(value).toString().replace("BTC", "ONX"));
            }
        });
        
        estatusColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                Coin value = tx.getValue(Main.bitcoin.wallet());
                String estatus = "Enviado";
                if(value.isPositive()) {
                    estatus = "Recibido";
                }
                else if (value.isNegative()) {
                   estatus = "Enviado";
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
                    descripcion = address.toString();
                }
                
                return new ReadOnlyObjectWrapper(descripcion);
            }
        });
        
        fechaColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> p) {
                Transaction tx = p.getValue();
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                return new ReadOnlyObjectWrapper(dateFormat.format(tx.getUpdateTime()));
            }
        });
        

    }

    private void showBitcoinSyncMessage() {
        syncItem = Main.instance.notificationBar.pushItem("Synchronising with the Onixcoin network", model.syncProgressProperty());
    }

    public void sendMoneyOut(ActionEvent event) {
        // Hide this UI and show the send money UI. This UI won't be clickable until the user dismisses send_money.
        Main.instance.overlayUI("send_money.fxml");
    }

    public void settingsClicked(ActionEvent event) {
        Main.OverlayUI<WalletSettingsController> screen = Main.instance.overlayUI("wallet_settings.fxml");
        screen.controller.initialize(null);
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
