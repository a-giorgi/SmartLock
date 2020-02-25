<?php
date_default_timezone_set("Europe/Rome");
$today = date("Y-m-d_H:i:s");

//log for debug
$file = fopen("log.txt","a");
$txt = fwrite($file,$today.PHP_EOL);


if((!isset($_REQUEST['status']))||(!isset($_REQUEST['code']))){
    echo "Parameters not set";
    exit();
}

include "sendNotification.php";

/*
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
*/

//status of the lock
$status = $_REQUEST['status'];

//device
$deviceCode = trim($_REQUEST['code']);

//parsing the status
if(trim($status)==='true'){
    $bool = 1;
    $description = "Lock status: OPEN";
}else{
    $bool = 0;
    $description = "Lock status: LOCKED";
}

//database queries
$query = "UPDATE device SET status = ?, online = '$today' WHERE codice = ?";
$queryCheckIfSend = "SELECT * FROM device WHERE codice = ?";


//setting up the database
include "databasePDO.php";
$handler = new DatabaseHandler();
$database = $handler->getDatabase();

/** getting old status to send notification if changed */
$stmtIf = $database->prepare($queryCheckIfSend);
if(!$stmtIf->execute(array($deviceCode))){
    exit();
}
$dataIf = $stmtIf->fetch(PDO::FETCH_NAMED);
/**-------------------------------*/

$stmt= $database->prepare($query);
if($stmt->execute(array($bool,$deviceCode))){
    echo "Done, sending notification...<br>";
    if($dataIf['status'] !== strval($bool)){ //if the status changed
        $queryFetchTokens = "SELECT * FROM users WHERE code = ? AND toggleNotifications = 1";
        $stmtNotif = $database->prepare($queryFetchTokens);
        if($stmtNotif->execute(array($deviceCode))){
            while($dataNofif = $stmtNotif->fetch(PDO::FETCH_NAMED)){
                $result = json_decode(sendNotification(trim($dataNofif['token']),$deviceCode, $description));
                if($result->status === 'EXCEPTION'){
                    //if Firebase return failure I'll delete the "broken" token
                    $queryDel = "DELETE FROM users WHERE token = ?";
                    $stmtDel = $database->prepare($queryDel);
                    $stmtDel->execute(array($result->token));
                }

            }
        }
    }
}else{
    exit("Database error");
}


