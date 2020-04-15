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
    .onWrite((snapshot, context) => {

        // Get a database reference
        var db = admin.database();

        // Grab the current value of what was written to the Realtime Database.
        const transactionKey = snapshot.after.key;

        var returned_promises;

        // DELETE event - deleting transaction
        if (!snapshot.after.exists()) {
            console.log("deleting transaction");

            returned_promises = db.ref("offers/").once("value")
                .then(function (offerSnapshot) {
                    var promises = [];
                    offerSnapshot.forEach(function(childSnapshot){
                        const offerID = childSnapshot.key;
                        const offer = childSnapshot.val();

                        //check if transaction ID in offer transactions ID
                        if ((transactionKey === offer.transactionID1) || (transactionKey === offer.transactionID2)) {
                            console.log("Found a match to cancel! offer:", offerID, offer);
                            promises.push(cancelOffer(offerID, db, true));
                            console.log("Offer updated - got CANCELLED");
                        }
                    });
                    return Promise.all(promises);
                })
                .catch(function (err) {
                    console.log("error in cancelRelevantOffers", err)
                });
            return returned_promises;
        }

        // Grab the current value of what was written to the Realtime Database.

        const transactionData = snapshot.after.val();
        const requestedCurrency = transactionData.myCurrency;
        const requestedAmount = transactionData.requestedAmount;
        const requestedCurrencies = transactionData.requestedCurrencies;
        const userID = transactionData.userId;


        console.log('Retreived transaction data:', transactionKey, transactionData);


        // UPDATE event - updating existing transaction
        if (snapshot.before.exists()) {

            // get all offers from given transaction and then run update logic
            returned_promises = getAllOffersFromTransaction(transactionKey, db, snapshot);

            // You must return a Promise when performing asynchronous tasks inside a Functions such as
            // writing to the Firebase Realtime Database.
            // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
            return returned_promises;
        }


        // CREATE event - new transaction
        else {

            returned_promises = db.ref("transactionRequests/").once("value")
                .then(function (snapshot) {
                    var promises = [];
                    snapshot.forEach(function(childSnapshot){
                            const transactionID = childSnapshot.key;
                            const transaction = childSnapshot.val();
                            // check if my currency is in transaction requested currency and transaction currency is in my currecies requests
                            if (requestedCurrencies.includes(transaction.myCurrency) && transaction.requestedCurrencies.includes(requestedCurrency) && checkIfInRadius(transactionData, transaction) && userID.localeCompare(transaction.userId) !== 0 )
                            {
                                console.log("Found a match! transaction:", transactionID, transaction);

                                console.log("loading DB, writing new offer");

                                // Creating a new offer in DB and update users data
                                var newPromises = addNewOfferToDatabase(requestedAmount, requestedCurrency, transaction.myCurrency, userID, transaction.userId, transactionKey, transactionID, db);
                                // extending promises with new promises
                                promises.push(...newPromises);

                                // sending push notifications to users
                                sendNotificationToUsers(userID, transaction.userId);
                            }
                        }
                    );
                    return Promise.all(promises);
                })
                .catch(function (err) {
                    console.log("error in findTransactionPairs - CREATE event", err)
                });


            // You must return a Promise when performing asynchronous tasks inside a Functions such as
            // writing to the Firebase Realtime Database.
            // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
            return returned_promises;
        }
    });

// exports.cancelRelevantOffers = functions.database.ref('/transactionRequests/{randId}')
//     .onDelete((snapshot, context) => {
//         // Grab the current value of what was written to the Realtime Database.
//         const transactionKey = snapshot.key;
//
//         // Get a database reference
//         var db = admin.database();
//
//         var offersRef = db.ref("offers/");
//         var returned_promises = offersRef.once("value")
//             .then(function (snapshot) {
//                 var promises = [];
//                 snapshot.forEach(function(childSnapshot){
//                     const offerID = childSnapshot.key;
//                     const offer = childSnapshot.val();
//
//                     //check if transaction ID in offer transactions ID
//                     if ((transactionKey === offer.transactionID1) || (transactionKey === offer.transactionID2)) {
//                         console.log("Found a match to cancel! offer:", offerID, offer);
//                         promises.push(cancelOffer(offerID, db, true));
//                         console.log("Offer updated - got CANCELLED");
//                     }
//                 });
//                 return Promise.all(promises);
//             })
//             .catch(function (err) {
//                 console.log("error in cancelRelevantOffers", err)
//             });
//         return returned_promises;
//     });


// This function cancel an offer. If the offer got already cancelled it will be deleted completely from DB
// return a Promise
function cancelOffer(offerID, db, delete_flag) {
    var return_promise = db.ref("offers/").child(offerID).once("value")
        .then(function (snapshot) {
            var offersRef = db.ref("offers/").child(offerID);
            var offer = snapshot.val();
            var promise;
            
            // if status is already CANCELLED and the delete flag is true - delete offer
            if (offer.status === "CANCELLED" && delete_flag) {
                promise = offersRef.remove()
                console.log("Offer got deleted from DB. offer ID:", offerID);
            }
            // otherwise, changing the offer status to CANCELLED
            else {
                promise = offersRef.update({
                    status: "CANCELLED"
                });
                console.log("Offer updated - got CANCELLED. offer ID:", offerID);
            }

            return promise;
        })
        .catch(function (err) {
            console.log("error in cancelOffer", err)
        });
    return return_promise;
}

// This function check if offer should be cancelled using the "to keep" list (keep ==> don't cancel)
// The function return all the promises for cancelling
function cancelOffersExceptKeepList(offerIDs, offerIDsToKeep, db) {
    var promises = [];
    for (const [index, offerID] of offerIDs.entries()) {
        // if index is not in "to keep" list, cancel offer
        if (!offerIDsToKeep.includes(index)) {
            promises.push(cancelOffer(offerID, db, false));
        }
    }
    return promises;
}


// This function find the index of an offer from offers list, that has a given transactionID value
// return -1 if not found
function findOfferIndex(offerValues, transactionID) {
    for (const [index, offer] of offerValues.entries()) {
        if (transactionID === offer.userID1 || transactionID === offer.userID2){
            console.log("offer already exists. Index:", index);
            return index;
        }
    }
    console.log("offer does not exists. Should create new one", index);
    return -1;
}


// This function return all the offers that has a given transcation ID in them
function getAllOffersFromTransaction(transactionKey, db, mainSnapshot) {
    var offersRef = db.ref("offers/");

    var offers = offersRef.once("value")
        .then(function (snapshot) {
            var offerKeys = [];
            var offerValues = [];
            snapshot.forEach(function(childSnapshot){
                const offerID = childSnapshot.key;
                const offer = childSnapshot.val();

                //check if transaction ID in offer transactions ID
                if ((transactionKey === offer.transactionID1) || (transactionKey === offer.transactionID2)) {
                    offerKeys.push(offerID);
                    offerValues.push(offerID);
                }

            });

            return updateOffer(offerKeys, offerValues, db, mainSnapshot);
        })
        .catch(function (err) {
            console.log("error in getAllOfferFromTransaction", err)
        });

    return offers;
}


// main offer update logic function
function updateOffer(offerIDs, offerValues, db, mainSnapshot) {

    // Grab the current value of what was written to the Realtime Database.
    const transactionKey = mainSnapshot.after.key;
    const transactionData = mainSnapshot.after.val();
    const requestedCurrency = transactionData.myCurrency;
    const requestedAmount = transactionData.requestedAmount;
    const requestedCurrencies = transactionData.requestedCurrencies;
    const userID = transactionData.userId;


    console.log("Queried all relevant offers from DB:", offerIDs);

    returned_promises = db.ref("transactionRequests/").once("value")
        .then(function (snapshot) {
            var promises = [];
            var offerIDsToKeep = [];
            snapshot.forEach(function(childSnapshot){
                const transactionID = childSnapshot.key;
                const transaction = childSnapshot.val();
                // check if my currency is in transaction requested currency and transaction currency is in my currecies requests
                if (requestedCurrencies.includes(transaction.myCurrency) && transaction.requestedCurrencies.includes(requestedCurrency) && checkIfInRadius(transactionData, transaction) && userID.localeCompare(transaction.userId) !== 0 ){
                    console.log("Found a potential match, checking if already exists. transaction:", transactionID, transaction);

                    var offerIndex = findOfferIndex(offerValues, transactionID);

                    // check if there's no offer that already match and just need an update
                    if (offerIndex !== -1) {
                        console.log("There's already an existing offer, won't create new one. OfferID:", offerIDs[offerIndex]);
                        offerIDsToKeep.push(offerIDs[offerIndex]);
                    }

                    // otherwise, create new offer
                    else {
                        var newPromises = addNewOfferToDatabase(requestedAmount, requestedCurrency, transaction.myCurrency, userID, transaction.userId, transactionKey, transactionID, db);
                        // extending promises with new promises
                        promises.push(...newPromises);
                    }

                    // sending push notifications to users
                    sendNotificationToUsers(userID, transaction.userId);
                }
            });

            // Cancel all offers that doesn't match anymore
            var cancelPromises = cancelOffersExceptKeepList(offerIDs, offerIDsToKeep, db);
            // extending promises with cancel promises
            promises.push(...cancelPromises);

            // making sure all transactions accure (writing to DB)
            return Promise.all(promises);
        })
        .catch(function (err) {
            console.log("error in findTransactionPairs - UPDATE event", err)
        });

}


// This function create new offer, adds it to DB and update the users in DB that they have new offer
function addNewOfferToDatabase(requestedAmount, requestedCurrency, myCurrency, userID, userID2, transactionKey, transactionID, db)
{
    var promises = [];
    var offerRef = db.ref("offers/").push({
        coinAmount1: requestedAmount,
        coinAmount2: -1,
        coinName1: requestedCurrency,
        coinName2: myCurrency,
        lastUpdated: "",
        status: "ACTIVE",
        userID1: userID,
        userID2: userID2,
        transactionID1: transactionKey,
        transactionID2: transactionID
    });
    promises.push(offerRef.once('value'));
    //offerRef.set();
    console.log("Offer written to DB, adding offer ID (key) to users");

    // saving offer ID to both users for future use
    var usersRef = db.ref("users/");
    // First user
    firstUserRef = usersRef.child(userID).child("offers").push(offerRef.key);
    promises.push(firstUserRef.once('value'));
    // Second user
    secondUserRef = usersRef.child(userID2).child("offers").push(offerRef.key);
    promises.push(secondUserRef.once('value'));

    console.log("Offer ID (key) added to users");

    return promises;
}


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