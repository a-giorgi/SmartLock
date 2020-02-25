<?php

class NotificationObj{
    public $body, $title;
    public function __construct($body,$title)
    {
        $this->body = $body;
        $this->title = $title;

    }
}


function sendNotification($token, $notificationTitle = 'SmartLock', $description ='Device status updated' ,$optionalData = 'NULL'){
    date_default_timezone_set("Europe/Rome");
	ini_set('max_execution_time', 0);

	//API access key from Google API's Console
	define( 'API_ACCESS_KEY', 'AAAAcIJVd34:APA91bGwWoezz3THZJjtNx-2ZOpLUBI0bvZJggn8pOOeXZpSfuET8VGWtgUeq-aqL1TAS3bU1edR1ZpavMr8SNBLZTeBfCcgApCY0QFZHbAajzwYMfMeYooZUbotVqiWqMpQg_wfNzKd' );


	/**----------------------------------------------------------------------
	*        PUSH NOTIFICATION FCM
	*----------------------------------------------------------------------*/

	try{

		$fields = array
				(
					'to' => $token,
                    'data' => new NotificationObj($description,$notificationTitle),

				);

		$headers = array
				(
					'Authorization: key=' . API_ACCESS_KEY,
					'Content-Type: application/json'
				);
		//Send Reponse To FireBase Server
		$chWAMFCM = curl_init();
		curl_setopt( $chWAMFCM,CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send' );
		curl_setopt( $chWAMFCM,CURLOPT_POST, true );
		curl_setopt( $chWAMFCM,CURLOPT_HTTPHEADER, $headers );
		curl_setopt( $chWAMFCM,CURLOPT_RETURNTRANSFER, true );
		curl_setopt( $chWAMFCM,CURLOPT_SSL_VERIFYPEER, false );
		curl_setopt( $chWAMFCM,CURLOPT_POSTFIELDS, json_encode( $fields ) );
		$result = curl_exec($chWAMFCM );
		curl_close( $chWAMFCM );

		$resultDecoded = json_decode($result);
        if($resultDecoded->failure=='1'){
            throw new Exception("firebase_failure");
        }
	}catch(Exception $firebaseException){
        return json_encode(array(
            'status' => 'EXCEPTION',
            'message' => 'La notifica non &egrave; stata inviata per un errore sul token relativo all\'utente selezionato o al server Firebase!',
            'token' => $token
        ));
	}

    return json_encode(array(
        'status' => 'OK',
        'message' => 'Notifica inviata correttamente'
    ));
	
}
	
	
	
	