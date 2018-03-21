
[![Pre-Release](https://jitpack.io/v/jestevez/onixcoinj-gui.svg)](https://jitpack.io/#jestevez/onixcoinj-gui)

Project status: Beta. Expect minor bugs and UI adjustments. Suitable for small scale production.

### Onixcoinj GUI

A desktop Hierarchical Deterministic Wallet (HDW) for Onixcoin using the Simplified Payment Verification (SPV) mode to provide very fast block chain synchronization.

The objective of this wallet is to be fast and simple.

### Main website

Pre-packaged installers are available in [Releases](https://github.com/jestevez/onixcoinj-gui/releases).

### Technologies

* Java 8 and JavaFX
* [onixcoinj](https://github.com/jestevez/onixcoinj) - Fork bitcoinj providing various blockchain protocol utilities

### Private as cash.

Onixcoinj allows you to hold your onixcoin in your own desktop. By connecting directly to the onixcoin network, you don’t have to rely on us to access your onix. We can't monitor your financial activity or control what you do with your money.

### Recover your wallet

Please take a moment to learn how HD wallets work. The nnemonic seed 15 words generated can be used to recover your wallet.

Onixcoinj-GUI use BIP39 Derivation Path is "m/0H/0"

If you want to try the operation you can visit this testing online tools https://iancoleman.io/bip39 and with 4 simple steps you can generate the private keys of your wallet

* Copy yours nnemonic seed 15 words, in the input **BIP39 Mnemonic** like "brisk wood symptom party betray ozone dad super beyond sea memory power pig business extra"
* In **Coin** Select ONX - Onixcoin
* In **Derivation Path** select the Tab **BIP32**
* In **Client** select **MultiBitHD**

For your security, do not share your seed, not save it in digital files or emails that can be accessed by malicious applications or virus. We recommend you write it down on a piece of paper and keep it in a safe place

### Getting Start

* Write your seed in a safe place.
* Secure your Onix! do not forget encrypt your wallet creating your password. Remember your wallet is stored in your Home directory and and can be accessed by anyone.
* Where is my wallet? you can find ${HOME}/onixcoinj-gui/ONIXCOIN-WALLET-BETA-info.onixcoin.production.wallet

### Creator's note

If you want to contribute this project send any donation to:

* ONX: XTo7XEAgPapkgJkgH6iR31J4cHBxwTgREe
* BTC: 1YeyoCJ2bS7Dhac6MjypFELXYhf3HLMgX

### Issues, Errors, Comments, Contributions

If you know how to make a pull request to contribute a fix, please write the correction and use a pull request to submit it for consideration against the develop branch. If you are making several changes, please use a separate commit for each to make it easier to cherry-pick or resolve conflicts. Otherwise, please submit an issue, explaining the error or comment.


