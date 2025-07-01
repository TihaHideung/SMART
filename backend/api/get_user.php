<?php
header('Content-Type: application/json');
include "koneksi.php";

$id_user = $_GET['id_user'] ?? '';

if (!empty($id_user)) {
    $stmt = $conn->prepare("SELECT id_user, name, email, password, profile_picture FROM users WHERE id_user = ?");

    $stmt->bind_param("i", $id_user);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        echo json_encode([
            "status" => "success",
            "user" => $row
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "User not found"]);
    }

    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Missing user ID"]);
}
