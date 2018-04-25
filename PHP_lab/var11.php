<?php
	$link = mysql_connect("localhost", "rk6stud", "rk6stud")
        or die("Could not connect : " . mysql_error());
 //   print "Connected successfully";
    /* ????? ?? my_database */
    mysql_select_db("femdb") or die("Could not select database");

    /* ?????????? SQL-??????? */
    //$query = "SELECT * FROM my_table";
    //$result = mysql_query($query) or die("Query failed : " . mysql_error());
	$starterx = 610;
	$startery = 360;
	$scale = 6;
	$counter_array;
function getPoints($id) {
	//global $pdo;
	//try {
	//	$sql = "SELECT x, y FROM nodes WHERE id = $id";
	//	return $pdo->query($sql);
	//} catch(PDOException $e) {
	//	exit();
	//}
	//$sql = "SELECT x, y FROM nodes WHERE id = $id";
	$sql = sprintf("SELECT x, y FROM nodes WHERE id = '%s'",
	      mysql_real_escape_string($id)); 
	$res =  mysql_query($sql) or die("Query failed : " . mysql_error());	
	return $res;
}

function getNodes() {
	//global $pdo;
	//try {
	//	$sql = "SELECT n1, n2, n3 FROM elements";
	//	return $pdo->query($sql);
	//} catch(PDOException $e) {
	//	exit();
	//}
	
	$sql = "SELECT n1, n2, n3 FROM elements";
	$res = mysql_query($sql) or die("Query failed : " . mysql_error());
	return $res;
}	

function getCounts($number){
	global $counter_array;
	for($i=1; $i < $number; $i++) {
	  $sql = sprintf("SELECT count(*) FROM elements where n1='%s' or n2='%s' or n3='%s'", mysql_real_escape_string($number), mysql_real_escape_string($number), mysql_real_escape_string($number)); 
	  $res =  mysql_query($sql) or die("Query failed : " . mysql_error());
	  $result = mysql_result($res, 0);
	  $counter_array[$i] = $result;
	}	
	var_dump($counter_array);
}
	//$result = getNodes();
	$i = 0;
	while($elements[$i] = mysql_fetch_array($result, MYSQL_ASSOC)) $i++;
	
	getCounts(30);
	//foreach ($elements as $value) {
	//	$counter_array[$value["n1"]]++;
	//	$counter_array[$value["n2"]]++;
	//	$counter_array[$value["n3"]]++;
	//}

	//header ('Content-Type: image/png');
	$im = imagecreatetruecolor(1280, 720);
	$color = imagecolorallocate($im, 255, 255, 255);
	foreach ($elements as $value) {
		$ps1 = getPoints($value["n1"]);
		$ps2 = getPoints($value["n2"]);
		$ps3 = getPoints($value["n3"]);
		$pointsN1 = mysql_fetch_array($ps1, MYSQL_ASSOC);
		$pointsN2 = mysql_fetch_array($ps2, MYSQL_ASSOC);
		$pointsN3 = mysql_fetch_array($ps3, MYSQL_ASSOC);
		imagestring($im, 4, $pointsN1["x"]*$scale+$starterx, $pointsN1["y"]*$scale+$startery, $counter_array[$value["n1"]]." ".$value["n1"], $color);
		imagestring($im, 4, $pointsN2["x"]*$scale+$starterx, $pointsN2["y"]*$scale+$startery, $counter_array[$value["n2"]]." ".$value["n2"], $color);
		imagestring($im, 4, $pointsN3["x"]*$scale+$starterx, $pointsN3["y"]*$scale+$startery, $counter_array[$value["n3"]]." ".$value["n3"], $color);
		imageline($im, $pointsN1["x"]*$scale+$starterx, $pointsN1["y"]*$scale+$startery, $pointsN2["x"]*$scale+$starterx, $pointsN2["y"]*$scale+$startery, $color);
		imageline($im, $pointsN2["x"]*$scale+$starterx, $pointsN2["y"]*$scale+$startery, $pointsN3["x"]*$scale+$starterx, $pointsN3["y"]*$scale+$startery, $color);
		imageline($im, $pointsN3["x"]*$scale+$starterx, $pointsN3["y"]*$scale+$startery, $pointsN1["x"]*$scale+$starterx, $pointsN1["y"]*$scale+$startery, $color);	
	}

	imagepng($im);
	imagedestroy($im);
?>
