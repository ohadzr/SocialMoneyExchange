// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();


// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

// exports.helloWorld = functions.https.onRequest((request, response) => {
//  console.log("Hello world!");
//  response.send("Hello from Firebase!");
// });


// Listens for new transactions added to /transactionRequests/:randId/
exports.findTransactionPairs = functions.database.ref('/transactionRequests/{randId}')
    .onUpdate((snapshot, context) => {
      // Grab the current value of what was written to the Realtime Database.
      const transactionData = snapshot.after.val();
      const requestedCurrency = transactionData.myCurrency;
	  const requestedAmount = transactionData.requestedAmount;
	  const requestedCurrencies = transactionData.requestedCurrencies;
	  const userID = transactionData.userId;
	  const transactionKey = snapshot.after.key;


      console.log('Retrieved transaction data:', transactionData);

      // Get a database reference
      var db = admin.database();

      var transactionsRef = db.ref("transactionRequests/");

      var returned_promises = transactionsRef.once("value")
          .then(function (snapshot) {
              var promises = [];
              snapshot.forEach(function(childSnapshot){
                  const transactionID = childSnapshot.key;
                  const transaction = childSnapshot.val();
                  // check if my currency is in transaction requested currency and transaction currency is in my currecies requests
                  if (requestedCurrencies.includes(transaction.myCurrency) && transaction.requestedCurrencies.includes(requestedCurrency) && checkIfInRadius(transactionData, transaction) && userID.localeCompare(transaction.userId) !== 0 ){
                      console.log("Found a match! transaction:", transactionID, transaction);

                      console.log("loading DB, writing new offer");

                      var offerRef = db.ref("offers/").push({
                          coinAmount1: requestedAmount,
                          coinAmount2: -1,
                          coinName1: requestedCurrency,
                          coinName2: transaction.myCurrency,
                          lastUpdated: "",
                          status: "ACTIVE",
                          userID1: userID,
                          userID2: transaction.userId,
                          transactionID1: transactionKey,
                          transactionID2: transactionID
                      });
                      promises.push(offerRef.once('value'));
                      //offerRef.set();
                      console.log("Offer written to DB, adding offer ID (key) to users");

                      // saving offer ID to both users for future use
                      var usersRef = db.ref("users/");
                      firstUserRef = usersRef.child(userID).child("offers").push(offerRef.key);
                      promises.push(firstUserRef.once('value'));
                      secondUserRef = usersRef.child(transaction.userId).child("offers").push(offerRef.key);
                      promises.push(secondUserRef.once('value'));
                      console.log("Offer ID (key) added to users");

                      // sending push notifications to users
                      sendNotificationToUsers(userID, transaction.userId);
                  }
              }
              );
              return Promise.all(promises);
          })
          .catch(function (err) {
              console.log("error in findTransactionPairs", err)
          });


      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to the Firebase Realtime Database.
      // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
      return returned_promises;
    });

exports.cancelRelevantOffers = functions.database.ref('/transactionRequests/{randId}')
    .onDelete((snapshot, context) => {
        // Grab the current value of what was written to the Realtime Database.
        const transactionData = snapshot.val();
        const userID = transactionData.userId;
        const transactionKey = snapshot.key;

        // Get a database reference
        var db = admin.database();

        var offersRef = db.ref("offers/");
        var returned_promises = offersRef.once("value")
            .then(function (snapshot) {
                var promises = [];
                snapshot.forEach(function(childSnapshot){
                    const offerID = childSnapshot.key;
                    const offer = childSnapshot.val();

                    //check if transaction ID in offer transactions ID
                    if ((transactionKey === offer.transactionID1) || (transactionKey === offer.transactionID2)) {
                        console.log("Found a match to cancel! offer:", offerID, offer);
                        var offerRef = db.ref("offers/"+offerID);
                        promises.push(offerRef.update({
                            status: "CANCELLED"
                        }));
                        console.log("Offer updated - got CANCELLED");
                    }
                });
                return Promise.all(promises);
            })
            .catch(function (err) {
                console.log("error in cancelRelevantOffers", err)
            });
    });

//This function takes in latitude and longitude of two location and returns the distance between them as the crow flies (in meters)
function calcCrow(lat1, lon1, lat2, lon2)
{
    var R = 6371; // km
    var dLat = toRad(lat2-lat1);
    var dLon = toRad(lon2-lon1);
    lat1 = toRad(lat1);
    lat2 = toRad(lat2);

    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c;
    return d*1000; //return in meters instead of km
}

// Converts numeric degrees to radians
function toRad(Value)
{
    return Value * Math.PI / 180;
}

function checkIfInRadius(transactionA, transactionB) {
    var latitudeA = transactionA.latitude;
    var latitudeB = transactionB.latitude;
    var longitudeA = transactionA.longitude;
    var longitudeB = transactionB.longitude;
    var radiusA = transactionA.radius;
    var radiusB = transactionB.radius;

    var distanceBetweenPoints = calcCrow(latitudeA, longitudeA, latitudeB, longitudeB);

    console.log("radiusA:", radiusA, " radiusB:", radiusB, " distanceBetweenPoints:", distanceBetweenPoints);

    // if radiusA - distanceBetweenPoints > 0, then pointB is inside the radius and should return true
    var result = ((radiusA-distanceBetweenPoints) > 0 && (radiusB-distanceBetweenPoints) > 0);
    if (result)
        console.log("locations are inside both radius");
    else
        console.log("locations are not in range");


    return result;
}

function sendNotificationToUsers(userID1, userID2) {
    // Get a database reference
    var db = admin.database();

    var token1Ref = db.ref("users/"+userID1+"/token");
    var token2Ref = db.ref("users/"+userID2+"/token");

    token1Ref.once("value", function(snapshot) {
        // console.log("snapshot1: ", snapshot);
        // console.log("token1: ", snapshot.val());
        var message = {
            notification: {
                title: "We've found a new offer for you!",
                body: 'Yay!!'
            }
        };
        var options = {
            priority: "high",
            timeToLive: 60 * 60 *24
        };

// Send a message to the device corresponding to the provided
// registration token.
        admin.messaging().sendToDevice(snapshot.val(), message, options)
            .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
            return
    })
    .catch((error) => {
            console.log('Error sending message:', error);
    });
    });

    token2Ref.once("value", function(snapshot) {
        // console.log("snapshot2: ", snapshot);
        // console.log("token2: ", snapshot.val());
        var message = {
            notification: {
                title: "We've found a new offer for you!",
                body: 'Yay!!'
            }
        };
        var options = {
            priority: "high",
            timeToLive: 60 * 60 *24
        };

// Send a message to the device corresponding to the provided
// registration token.
        admin.messaging().sendToDevice(snapshot.val(), message, options)
            .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
            return
    })
    .catch((error) => {
            console.log('Error sending message:', error);
    });
    });
}