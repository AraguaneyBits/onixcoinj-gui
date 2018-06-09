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
package info.onixcoin.tools;

import com.google.common.collect.ComparisonChain;
import info.onixcoin.api.OnixcoinInfoApi;
import info.onixcoin.desktop.Main;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletTransaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jestevez
 */
public class SweepWalletTool {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SweepWalletTool.class);

    /**
     * Generamos la billetera a barrar a partir de la llave privada y la
     * direccion
     *
     * @param address
     * @param priv
     * @return
     * @throws Exception
     */
    public static Wallet prepareWalletToSweep(String address, String priv) throws Exception {
        Address addressToSweep = Address.fromBase58(Main.params, address);
        // buscamos la utxo
        Set<UTXO> utxos = requestAddressUtxo(addressToSweep);
        // generamos la billetera a barrer usando la clave privada y los utxo
        Wallet walletToSweep = createSweepWallet(priv, utxos);
        return walletToSweep;
    }

    /**
     * Enviar el saldo de la billetera de papel a una de nuestras direcciones
     * del monedero
     *
     * @param walletToSweep
     * @return
     * @throws Exception
     */
    public static String sweepWallet(Wallet walletToSweep) throws Exception {

        // Vaciamos la billetera
        final SendRequest sendRequest = SendRequest.emptyWallet(Main.bitcoin.wallet().freshReceiveAddress());
        // TODO implementar manejo de fees
        Coin fee = Transaction.DEFAULT_TX_FEE;
        sendRequest.feePerKb = fee;

        // Pereparamos la transaccion a ser enviada
        Transaction transaction = sendCoinsOffline(sendRequest, walletToSweep);
        // Enviamos la tx a la red onix
        String txid = broadcastTx(transaction);

        return txid;
    }

    /**
     * Ampliamos la funcionalidad de la transcciones de bitcoinj
     */
    private static class FakeTransaction extends Transaction {

        private final Sha256Hash hash;

        public FakeTransaction(final NetworkParameters params, final Sha256Hash hash) {
            super(params);
            this.hash = hash;
        }

        @Override
        public Sha256Hash getHash() {
            return hash;
        }
    }

    /**
     * Retorna la coleccion de entradas no gastadas de una direccion
     *
     * @param address
     * @return
     * @throws IOException
     */
    private static Set<UTXO> requestAddressUtxo(final Address address) throws IOException {
        Set<UTXO> utxos = new HashSet<>();
        JSONArray array = OnixcoinInfoApi.addrUtxo(address.toString());
        int i = 0;
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject utxo = (JSONObject) o;
                BigDecimal amount = utxo.getBigDecimal("amount");

                String txid = utxo.getString("txid");
                JSONObject tx = OnixcoinInfoApi.tx(txid);
                int size = tx.getInt("size");

                final Sha256Hash utxoHash = Sha256Hash.wrap(txid);
                final int utxoIndex = i;
                final Coin utxoValue = Coin.valueOf(amount.multiply(new BigDecimal("100000000")).longValue());
                final Script script = ScriptBuilder.createOutputScript(address);
                final int height = size;
                final UTXO utxoj = new UTXO(utxoHash, utxoIndex, utxoValue, height, false, script);

                utxos.add(utxoj);
                i++;
            }
        }
        return utxos;
    }

    /**
     * Retorna una transaccion offline a partir del SendRequest
     *
     * @param sendRequest
     * @param wallet
     * @return
     * @throws Exception
     */
    private static Transaction sendCoinsOffline(final SendRequest sendRequest, Wallet wallet) throws Exception {
        try {
            org.bitcoinj.core.Context.propagate(new Context(Main.params));
            LOG.debug("#################################################################################");
            LOG.debug("billetera antes de barrer  {} ", wallet);
            LOG.debug("#################################################################################");
            LOG.debug("Saldo: {}", wallet.getBalance());
            LOG.debug("sending: {} ", sendRequest);
            //FIXME Enviar en un hilo
            final Transaction transaction = wallet.sendCoinsOffline(sendRequest);
            LOG.debug("send successful, transaction committed: {}" + transaction.getHashAsString());
            LOG.debug("transaction", transaction);
            LOG.debug("#################################################################################");
            LOG.debug("billetera despues de barrer  {} ", wallet);
            LOG.debug("#################################################################################");
            LOG.debug("Saldo: {}", wallet.getBalance());
            LOG.debug("fee: {}", transaction.getFee());
            return transaction;
        } catch (final InsufficientMoneyException x) {
            final Coin missing = x.missing;
            if (missing != null) {
                LOG.error("send failed, {} missing", missing.toFriendlyString());
            } else {
                LOG.error("send failed, insufficient coins");
            }
            throw x;
        } catch (final ECKey.KeyIsEncryptedException x) {
            LOG.error("send failed, key is encrypted: {}", x.getMessage());
            throw x;
        } catch (final KeyCrypterException x) {
            LOG.error("send failed, key crypter exception: {}", x.getMessage());
            final boolean isEncrypted = wallet.isEncrypted();
            LOG.error("isEncrypted {}", isEncrypted);
            throw x;
        } catch (final Wallet.CouldNotAdjustDownwards x) {
            LOG.error("send failed, could not adjust downwards: {}", x.getMessage());
            throw x;
        } catch (final Wallet.CompletionException x) {
            LOG.error("send failed, cannot complete: {}", x.getMessage());
            throw x;
        }

    }

    /**
     * Retorna una billetera a partir de la llave privada y las transacciones no
     * gastadas
     *
     * @param priv
     * @param utxos
     * @return
     */
    private static Wallet createSweepWallet(String priv, Set<UTXO> utxos) {
        // Filtra los UTXO que ya hemos gastado y ordena el resto.
        final Set<Transaction> walletTxns = Main.bitcoin.wallet().getTransactions(false);
        LOG.debug("walletTxnsv " + walletTxns);
        final Set<UTXO> sortedUtxos = new TreeSet<>(UTXO_COMPARATOR);
        for (final UTXO utxo : utxos) {
            if (!utxoSpentBy(walletTxns, utxo)) {
                sortedUtxos.add(utxo);
            }
        }
        // Transacción falsa que financia la billetera que vamos a barrer.
        final Map<Sha256Hash, Transaction> fakeTxns = new HashMap<>();
        for (final UTXO utxo : sortedUtxos) {
            Transaction fakeTx = fakeTxns.get(utxo.getHash());
            if (fakeTx == null) {
                fakeTx = new FakeTransaction(Main.params, utxo.getHash());
                fakeTx.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
                fakeTxns.put(fakeTx.getHash(), fakeTx);
            }
            final TransactionOutput fakeOutput = new TransactionOutput(Main.params, fakeTx,
                    utxo.getValue(), utxo.getScript().getProgram());
            // Llene las tx de salida según sea necesario.
            while (fakeTx.getOutputs().size() < utxo.getIndex()) {
                fakeTx.addOutput(new TransactionOutput(Main.params, fakeTx,
                        Coin.NEGATIVE_SATOSHI, new byte[]{}));
            }
            // Agregue la tx real que gastaremos más adelante.
            fakeTx.addOutput(fakeOutput);

        }
        // crear billetera con llave privada
        ECKey key = DumpedPrivateKey.fromBase58(Main.params, priv).getKey();
        final KeyChainGroup group = new KeyChainGroup(Main.params);
        group.importKeys(key);
        Wallet walletToSweep = new Wallet(Main.params, group);
        // Cargar las tx a la billetera
        for (final Transaction tx : fakeTxns.values()) {
            walletToSweep.addWalletTransaction(new WalletTransaction(WalletTransaction.Pool.UNSPENT, tx));
        }

        return walletToSweep;
    }

    /**
     * Recibimos una transacción lista para se difundida en la red y retorna el
     * txid
     *
     * @param transaction
     * @return
     * @throws IOException
     */
    private static String broadcastTx(Transaction transaction) throws IOException {
        // Serializamos la tx para tener el rawtx a enviar
        byte[] serialize = transaction.bitcoinSerialize();
        String rawtx = Utils.HEX.encode(serialize);
        LOG.debug("rawtx {} ", rawtx);

        // Enviar la transaccion serializada por el api de onixcoin.info
        JSONObject jSONObject = OnixcoinInfoApi.txSend(rawtx);
        String txid = jSONObject.getString("txid");
        LOG.debug("txid {} ", txid);
        return txid;
    }

    private static final Comparator<UTXO> UTXO_COMPARATOR = new Comparator<UTXO>() {
        @Override
        public int compare(final UTXO lhs, final UTXO rhs) {
            return ComparisonChain.start().compare(lhs.getHash(), rhs.getHash()).compare(lhs.getIndex(), rhs.getIndex())
                    .result();
        }
    };

    private static boolean utxoSpentBy(final Set<Transaction> transactions, final UTXO utxo) {
        for (final Transaction tx : transactions) {
            for (final TransactionInput input : tx.getInputs()) {
                final TransactionOutPoint outpoint = input.getOutpoint();
                if (outpoint.getHash().equals(utxo.getHash()) && outpoint.getIndex() == utxo.getIndex()) {
                    return true;
                }
            }
        }
        return false;
    }

}
