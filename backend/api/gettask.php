<?php
header('Content-Type: application/json');
include "koneksi.php";

$id_user = $_POST['id_user'] ?? ''; // ← GANTI $_GET jadi $_POST

$response = [];

if ($id_user) {
    $stmt = $conn->prepare("SELECT * FROM tasks WHERE id_user = ?");
    $stmt->bind_param("i", $id_user);
    $stmt->execute();
    $result = $stmt->get_result();

    $tasks = [];
    while ($row = $result->fetch_assoc()) {
        $tasks[] = $row;
    }

    $response['status'] = 'success';
    $response['tasks'] = $tasks;
} else {
    $response['status'] = 'error';
    $response['message'] = 'ID user kosong';
}
file_put_contents("debug_gettask.txt", print_r($_POST, true));
echo json_encode($response);
