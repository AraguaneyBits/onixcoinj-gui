Build status: [![Build Status](https://travis-ci.org/jestevez/onixcoinj-gui.svg?branch=master)](https://travis-ci.org/jestevez/onixcoinj-gui) 

Project status: Beta. Expect minor bugs and UI adjustments. Suitable for small scale production.

### Onixcoinj GUI

A desktop Hierarchical Deterministic Wallet (HDW) for Onixcoin using the Simplified Payment Verification (SPV) mode to provide very fast block chain synchronization.

The objective of this purse is fast and very simple to use your onix at any time.

### Main website

Pre-packaged installers are available from the [Onixcoin.info website](https://www.onixcoin.info/wallet).

### Technologies

* Java 8 and JavaFX
* [Onixcoinj](https://github.com/jestevez/onixcoinj) - Fork bitcoinj providing various blockchain protocol utilities

### Private as cash.

Onixcoinj allows you to hold your onix in your own desktop. By connecting directly to the onixcoin network, you don’t have to rely on us to access your onix. We can't monitor your financial activity or control what you do with your money.

### Recover your wallet

Please take a moment to learn how HD wallets work. The nnemonic seed 15 words generated can be used to recover your wallet at any time.

Onixcoinj-GUI use BIP39 Derivation Path is m/0H/0

If you want to try the operation you can visit this testing online tools https://iancoleman.io/bip39/ and with 4 simple steps you can generate the private keys of your wallet

1.- Copy yours nnemonic seed 15 words in the input BIP39 Mnemonic like "brisk wood symptom party betray ozone dad super beyond sea memory power pig business extra"
2.- In Coin Select ONX - Onixcoin
3.- In Derivation Path select Tab  BIP32
4.- In Client select MultiBitHD

For your safety, do not share your seed, save it in digital files or emails that can be accessed by malicious applications or viruses. We recommend you write it down on a piece of paper and keep it in a safe place