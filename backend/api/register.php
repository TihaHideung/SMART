<?php
include "koneksi.php";

$name = $_POST['name'] ?? '';
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

$response = [];

if (empty($name) || empty($email) || empty($password)) {
    $response['status'] = 'error';
    $response['message'] = 'Field tidak boleh kosong';
} else {
    // Cek apakah email sudah digunakan
    $cek = $conn->query("SELECT * FROM users WHERE email='$email'");
    if ($cek->num_rows > 0) {
        $response['status'] = 'error';
        $response['message'] = 'Email sudah terdaftar';
    } else {
        $insert = $conn->query("INSERT INTO users (name, email, password) VALUES ('$name', '$email', '$password')");
        if ($insert) {
            $user_id = $conn->insert_id; // ambil id_user terakhir yang baru dibuat
            $response['status'] = 'success';
            $response['message'] = 'Registrasi berhasil';
            $response['id_user'] = $user_id; // ⬅️ penting untuk dikirim ke Android
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Gagal menyimpan ke database';
        }
    }
}

header('Content-Type: application/json');
echo json_encode($response);
