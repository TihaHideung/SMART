<?php
include "koneksi.php";

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

$response = array();

$query = "SELECT * FROM users WHERE email='$email' AND password='$password'";
$result = $conn->query($query);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $response['status'] = "success";
    $response['message'] = "Login berhasil";
    $response['user'] = array(
        "id_user" => $row['id_user'],
        "name" => $row['name'],
        "email" => $row['email']
    );
} else {
    $response['status'] = "error";
    $response['message'] = "Email atau password salah";
}

echo json_encode($response);
