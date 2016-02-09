# Summoners (Android application)
Android app that works with the [League of Legends API](https://developer.riotgames.com/api/methods) and
is a powerful tool for the summoners. The developing of the app started months ago. The app passed many
phases until it has become a stable beta version!

# Functionality
- Create and maintain a list of summoners.
- See the basic information of a summoner and his/her recent match history.
- See explicit information of a match of their recent match history.
- Check the general statistics of a summoner.
- Check the league/s of a summoner.
- See in-game info of a summoner.

# Asynchronous threads.
All the requests to the Riot Servers are handled by asynchronous task, so that the
UI never gets stuck and the error control is correctly managed.

# How to run / test it.
This is an android studio project, so can take the sources and import then.
1. Do a git pull.
2. Import the sources.
You need to have android studio installed on you computer to do this. You should
also insert a valid API Developer Key in the class StaticKeyData.java before
doing anything else.

But this maybe a bit complex, so, in order to use the app easily, you can
install on your device the .apk file which is in the /app of the repository
(that is compiled with a valid key - no modifications are needed!).

Have fun :)
