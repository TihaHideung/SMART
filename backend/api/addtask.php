<?php
header('Content-Type: application/json');
include 'koneksi.php';

// Debug isi POST
file_put_contents("debug_post.txt", print_r($_POST, true));

$title       = $_POST['title'] ?? '';
$description = $_POST['description'] ?? '';
$due_date    = $_POST['due_date'] ?? '';
$due_time    = $_POST['due_time'] ?? '';
$id_user     = $_POST['id_user'] ?? '';

$response = [];

if ($title && $due_date && $due_time && $id_user) {
    $stmt = $conn->prepare("
        INSERT INTO tasks (id_user, title, description, due_date, due_time)
        VALUES (?, ?, ?, ?, ?)
    ");
    $stmt->bind_param("issss", $id_user, $title, $description, $due_date, $due_time);

    if ($stmt->execute()) {
        $response['status']  = 'success';
        $response['message'] = 'Task berhasil ditambahkan';
        $response['id_task'] = $stmt->insert_id;
    } else {
        $response['status']  = 'error';
        $response['message'] = 'Gagal tambah task: ' . $stmt->error;
    }

    $stmt->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Field tidak lengkap';
}

echo json_encode($response);
