<?php
Class DatabaseHandler{
    private $database;
    private static $databasePassword = 'YOUR_DB_PSW';
    private static $databaseName = 'YOUR_DB_NAME';
    private static $databaseHost = 'YOUR_DB_HOST';
    private static $databaseUser = 'YOUR_DB_USER';
    private $error = false;
    private $message = "";
    public function getDatabase(){
        if($this->error){
            throw new Exception($this->message);
        }
        return $this->database;
    }
    public function __construct(){
        try {
            date_default_timezone_set("Europe/Rome");
            $this->database = new PDO("mysql:host=".self::$databaseHost.";dbname=".self::$databaseName.";charset=utf8", self::$databaseUser, self::$databasePassword, array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC));
        } catch (PDOException $pe) {
            $this->error;
            $this->message = "Could not connect to the database ".self::$databaseName.": " . $pe->getMessage();
            die("Could not connect to the database ".self::$databaseName.": " . $pe->getMessage());

        }

    }
}
