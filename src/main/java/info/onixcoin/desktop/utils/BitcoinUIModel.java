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

package info.onixcoin.desktop.utils;

import eu.hansolo.enzo.notification.Notification;
import eu.hansolo.enzo.notification.NotificationBuilder;
import eu.hansolo.enzo.notification.NotifierBuilder;
import info.onixcoin.desktop.Main;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.core.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Date;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.LoggerFactory;

/**
 * A class that exposes relevant bitcoin stuff as JavaFX bindable properties.
 */
public class BitcoinUIModel {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BitcoinUIModel.class);
    private SimpleObjectProperty<Address> address = new SimpleObjectProperty<>();
    private SimpleObjectProperty<Coin> balance = new SimpleObjectProperty<>(Coin.ZERO);
    private SimpleDoubleProperty syncProgress = new SimpleDoubleProperty(-1);
    private ProgressBarUpdater syncProgressUpdater = new ProgressBarUpdater();
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    public BitcoinUIModel() {
    }

    public BitcoinUIModel(Wallet wallet) {
        setWallet(wallet);
    }

    private Notification.Notifier notifier;
    public final void setWallet(Wallet wallet) {
        wallet.addChangeEventListener(new WalletChangeEventListener() {
            @Override
            public void onWalletChanged(Wallet wallet) {
                update(wallet);
            }
            
        });
        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
//                System.out.println("\nReceived tx " + tx.getHashAsString());
//                System.out.println(tx.toString());
                
                try {
                    
                    String body = "Pago recibido Tx: " + tx.getHashAsString();
                    Coin value = tx.getValue(wallet);
                    String estatus;
                    if(value.isPositive()) {
                         estatus = Main.resourceBundle.getString("notification.received");
                    }
                    else {
                         estatus = Main.resourceBundle.getString("notification.sending");
                    }
                    BigDecimal satoshis = new BigDecimal(value.value);
                    BigDecimal amountBTC = satoshis.divide(new BigDecimal(100000000));

                    body += " ONX: " + satoshis;
                    body += " balance:" + wallet.getBalance().toFriendlyString();
                
//                    // Create a custom Notification without icon
//                    Notification info = new Notification(estatus, amountBTC.toString() +" ONX", new Image(getClass().getResourceAsStream("/info/onixcoin/desktop/onixcoin.png")));
//                    
//                    // Show the custom notification
//                    Notification.Notifier.INSTANCE.notify(info);
                    
                        notifier = NotifierBuilder.create()
                                .popupLocation(Pos.BOTTOM_RIGHT)
                                .popupLifeTime(Duration.millis(10_000))
                                .height(100.0d).owner(Main.instance.mainWindow)
                                .build();

                        Notification notification = NotificationBuilder.create()   
                            .title(estatus)
                            .message(amountBTC.toString() +" ONX")
                            .image(Notification.SUCCESS_ICON)
                            .build();
                        
                        
                        Platform.runLater(() -> notifier.notify(notification));

                        notifier.setOnHideNotification(event ->  notifier.stop());
                        
                        
                } catch (Exception e) {
                    LOG.error("onCoinsReceived {}", e);
                }
                    
                update(wallet);
            }
        });
        update(wallet);
    }

    private void update(Wallet wallet) {
        balance.set(wallet.getBalance());
        address.set(wallet.currentReceiveAddress());
        transactions.setAll(wallet.getTransactionsByTime());
    }

    private class ProgressBarUpdater extends DownloadProgressTracker {
        @Override
        protected void progress(double pct, int blocksLeft, Date date) {
            super.progress(pct, blocksLeft, date);
            Platform.runLater(() -> syncProgress.set(pct / 100.0));
        }

        @Override
        protected void doneDownload() {
            super.doneDownload();
            Platform.runLater(() -> syncProgress.set(1.0));
        }
    }

    public DownloadProgressTracker getDownloadProgressTracker() { return syncProgressUpdater; }

    public ReadOnlyDoubleProperty syncProgressProperty() { return syncProgress; }

    public ReadOnlyObjectProperty<Address> addressProperty() {
        return address;
    }

    public ReadOnlyObjectProperty<Coin> balanceProperty() {
        return balance;
    }

    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }
}
