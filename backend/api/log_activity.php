<?php
require_once 'koneksi.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $id_user = $_POST['id_user'];
    $activity = $_POST['activity'];

    $stmt = $conn->prepare("INSERT INTO loguser (id_user, activity) VALUES (?, ?)");
    $stmt->bind_param("is", $id_user, $activity);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Log tersimpan"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Gagal menyimpan log"]);
    }

    $stmt->close();
    $conn->close();
}
