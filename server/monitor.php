<?php
date_default_timezone_set("Europe/Rome");
$today = date("Y-m-d_H:i:s");

//log
$file = fopen("log2.txt","a");
$txt = fwrite($file,$today.PHP_EOL);

/*
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);*/

if(!isset($_REQUEST['code'])){
    echo "ERROR";
    exit();
}

$deviceCode = trim($_REQUEST['code']);
$query = "SELECT count(*) as occurrences, status, online FROM device WHERE codice = ?;"; //I have unicity constraint on field "codice"

//setting up the database
include "databasePDO.php";
$handler = new DatabaseHandler();
$database = $handler->getDatabase();

$stmt= $database->prepare($query);

if($stmt->execute(array($deviceCode))){
    $row = $stmt->fetch(PDO::FETCH_NAMED);
    if($row['occurrences']<1){ //there is no device with that code
        echo "NODEVICE";
        exit();
    }
    $lastSeen = strtotime(str_replace('_', ' ', $row['online'])); //using the field 'online' to determine whether the device is online or not
    if((time()-$lastSeen)>200){
        echo "OFFLINE";
    }else {
        //now parsing the field "status"
        if ($row['status'] === '0') {
            echo "LOCKED";
        } else {
            echo "OPEN";
        }
    }
}else{
    //Database error
    echo "ERROR";
}

