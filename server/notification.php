<?php
date_default_timezone_set("Europe/Rome");
$file = fopen("logNotif.txt","a");
$today = date("Y-m-d_H:i:s");
$txt = fwrite($file,$today.$_REQUEST['token'].PHP_EOL);

if((!isset($_REQUEST['token']))||(!isset($_REQUEST['device']))||(!isset($_REQUEST['toggle']))){
    echo "Parameters not set";
    exit();
}
/*
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
*/

//notification subscription
$toggle = trim($_REQUEST['toggle']);

//device code
$deviceCode = trim($_REQUEST['device']);

//smartphone token
$token = trim($_REQUEST['token']);

//set up the database
include "databasePDO.php";
$handler = new DatabaseHandler();
$database = $handler->getDatabase();


if($toggle==='S'){
    $bool = 1;
}else{
    $bool = 0;
}

//check if the token is into the database
$querySelectToken = "SELECT count(*) as occurrences FROM users WHERE code = ? AND token = ? ";
$stmtSelectToken = $database->prepare($querySelectToken);
if(!$stmtSelectToken->execute(array($deviceCode,$token))){
    echo "error";
    exit();
}
$dataSelectToken = $stmtSelectToken->fetch(PDO::FETCH_NAMED);
if($dataSelectToken['occurrences']>0){ //if the token exists into the db I'll make an update
    $queryNotif = "UPDATE users SET toggleNotifications = $bool WHERE code = ? AND token = ?";
}else{ //otherwise an insert
    $queryNotif = "INSERT INTO users (toggleNotifications, code, token) VALUES ($bool, ?,?)";
}

//preparing and running the query for the notifications
$stmtNotif = $database->prepare($queryNotif);
if(!$stmtNotif->execute(array($deviceCode,$token))){
    echo 'error';
    exit();
}
echo "Done";
