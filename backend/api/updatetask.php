<?php
require_once 'koneksi.php'; // Koneksi ke database

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Ambil data dari request
    $id_task     = $_POST['id_task'] ?? '';
    $title       = $_POST['title'] ?? '';
    $description = $_POST['description'] ?? '';
    $due_date    = $_POST['due_date'] ?? '';
    $due_time    = $_POST['due_time'] ?? '';

    // Validasi sederhana
    if (empty($id_task) || empty($title)) {
        echo json_encode([
            "status" => "error",
            "message" => "ID task dan judul tidak boleh kosong"
        ]);
        exit;
    }

    // Query update
    $query = "UPDATE tasks SET title = ?, description = ?, due_date = ?, due_time = ? WHERE id_task = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ssssi", $title, $description, $due_date, $due_time, $id_task);

    if ($stmt->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "Task berhasil diperbarui"
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Gagal memperbarui task"
        ]);
    }

    $stmt->close();
    $conn->close();
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Metode tidak diperbolehkan"
    ]);
}
