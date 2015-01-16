# MiddleMan - Money Transfer Application
A concept application that allows people to send money from Credit/Debit cards or PayPal Balance to M-Pesa or Bank Accounts
in Tanzania.

<b>Idea And How it Works</b>
The Idea came when I was sending money to a friend via Western-Union and I realized it wasn't fast and VERY expensive.
So I started writing Middleman to help other people overcome those barriers

The Application Works this way, It's a bit hacky and can be improved:

1. Developer has to have two accounts, one in the US(preferrably) and another one in the country he operates e.g Tanzania
</br>
2. Sender sends money which will be put in the US account, developer delivers the equal amount minus charges to the receiver
using a local account in Tanzania


<b>Security</b>

The Service is as secure as paypal is, all transactions are handled by PayPal, the application does not store any payment details

<b>Devices To Test With</b>

Tested with: Nexus 5, Android 5.0(Lollipop)

The UI values are pretty much hardcoded for the Nexus 5 which means it should show well on any 4.7" to 5.2" 1080p screens.

<b>Not Included</b>:
Implementation on M-Pesa and Bank Account delivery was on early alpha when I decided to cancell this project and that part will
not be included in this repository
