<?php
header('Content-Type: application/json');
include 'koneksi.php';

$id_task = intval($_POST['id_task'] ?? 0);
$response = [];

if ($id_task > 0) {
    $stmt = $conn->prepare("DELETE FROM tasks WHERE id_task = ?");
    $stmt->bind_param("i", $id_task);

    if ($stmt->execute()) {
        $response['status'] = 'success';
        $response['message'] = 'Task berhasil dihapus';
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Gagal hapus task: ' . $stmt->error;
    }

    $stmt->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'ID task tidak valid';
}

echo json_encode($response);
